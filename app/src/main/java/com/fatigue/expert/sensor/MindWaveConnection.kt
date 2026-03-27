package com.fatigue.expert.sensor

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import com.neurosky.AlgoSdk.NskAlgoDataType
import com.neurosky.AlgoSdk.NskAlgoSdk
import com.neurosky.AlgoSdk.NskAlgoType
import com.neurosky.connection.ConnectionStates
import com.neurosky.connection.DataType.MindDataType
import com.neurosky.connection.EEGPower
import com.neurosky.connection.TgStreamHandler
import com.neurosky.connection.TgStreamReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Bluetooth connection to NeuroSky MindWave Mobile 2.
 * Uses TgStreamReader directly — band powers, attention, meditation
 * are sent by the TGAM chip over serial every ~1 second.
 * Raw EEG is streamed at 512 Hz for waveform plotting.
 */
class MindWaveConnection(context: Context) {

    companion object {
        private const val TAG = "MindWave"
        const val MINDWAVE_DEVICE_NAME = "MindWave Mobile"
        const val RAW_BUFFER_SIZE = 512 // 1 second of raw EEG at 512 Hz
    }

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR
    }

    data class MindWaveData(
        val attention: Int = 0,          // 0-100, from TGAM eSense
        val meditation: Int = 0,         // 0-100, from TGAM eSense
        val signalQuality: Int = 200,    // 0=good, 200=no contact
        // Band powers (direct from TGAM chip, absolute values)
        val delta: Int = 0,
        val theta: Int = 0,
        val lowAlpha: Int = 0,
        val highAlpha: Int = 0,
        val lowBeta: Int = 0,
        val highBeta: Int = 0,
        val lowGamma: Int = 0,
        val middleGamma: Int = 0,
        // Derived
        val alpha: Float = 0f,   // lowAlpha + highAlpha (normalized)
        val beta: Float = 0f,    // lowBeta + highBeta (normalized)
        // Blink detection (from NskAlgoSdk)
        val blinkStrength: Int = 0,      // 0-255
        val blinkDetected: Boolean = false,
        val timestamp: Long = System.currentTimeMillis()
    )

    /** Raw EEG waveform buffer (512 samples = 1 second at 512 Hz) */
    data class RawEegBuffer(
        val samples: ShortArray = ShortArray(RAW_BUFFER_SIZE),
        val index: Int = 0,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _data = MutableStateFlow(MindWaveData())
    val data: StateFlow<MindWaveData> = _data.asStateFlow()

    private val _rawEeg = MutableStateFlow(RawEegBuffer())
    val rawEeg: StateFlow<RawEegBuffer> = _rawEeg.asStateFlow()

    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private var tgStreamReader: TgStreamReader? = null
    private val rawBuffer = ShortArray(RAW_BUFFER_SIZE)
    private var rawIndex = 0
    private var nskAlgoSdk: NskAlgoSdk? = null

    init {
        // Set up NskAlgoSdk for blink detection only
        try {
            System.loadLibrary("NskAlgo")
            val sdk = NskAlgoSdk()
            nskAlgoSdk = sdk
            sdk.setOnEyeBlinkDetectionListener { strength ->
                Log.d(TAG, "Blink detected: strength=$strength")
                _data.value = _data.value.copy(
                    blinkStrength = strength,
                    blinkDetected = true,
                    timestamp = System.currentTimeMillis()
                )
            }
            val algoTypes = NskAlgoType.NSK_ALGO_TYPE_BLINK.value
            val initResult = NskAlgoSdk.NskAlgoInit(algoTypes, "")
            Log.i(TAG, "NskAlgoInit (blink only) result: $initResult")
            NskAlgoSdk.NskAlgoStart(false)
        } catch (e: Exception) {
            Log.e(TAG, "NskAlgo blink init failed: ${e.message}")
        }
    }

    private val streamCallback = object : TgStreamHandler {
        override fun onStatesChanged(connectionStates: Int) {
            when (connectionStates) {
                ConnectionStates.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected — starting stream")
                    tgStreamReader?.start()
                    _connectionState.value = ConnectionState.CONNECTED
                }
                ConnectionStates.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected")
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
                ConnectionStates.STATE_GET_DATA_TIME_OUT,
                ConnectionStates.STATE_ERROR,
                ConnectionStates.STATE_FAILED -> {
                    Log.e(TAG, "Connection error: $connectionStates")
                    _connectionState.value = ConnectionState.ERROR
                    disconnect()
                }
            }
        }

        override fun onRecordFail(flag: Int) {
            Log.e(TAG, "Record fail: $flag")
        }

        override fun onChecksumFail(payload: ByteArray?, length: Int, checksum: Int) {}

        override fun onDataReceived(datatype: Int, data: Int, obj: Any?) {
            when (datatype) {
                MindDataType.CODE_ATTENTION -> {
                    Log.d(TAG, "Attention: $data")
                    _data.value = _data.value.copy(
                        attention = data,
                        timestamp = System.currentTimeMillis()
                    )
                }
                MindDataType.CODE_MEDITATION -> {
                    Log.d(TAG, "Meditation: $data")
                    _data.value = _data.value.copy(
                        meditation = data,
                        timestamp = System.currentTimeMillis()
                    )
                }
                MindDataType.CODE_POOR_SIGNAL -> {
                    _data.value = _data.value.copy(
                        signalQuality = data,
                        blinkDetected = false, // reset blink flag
                        timestamp = System.currentTimeMillis()
                    )
                    // Feed to algo SDK for blink detection
                    try { NskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_PQ.value, shortArrayOf(data.toShort()), 1) } catch (_: Exception) {}
                }
                MindDataType.CODE_EEGPOWER -> {
                    // Band powers sent directly by TGAM chip every ~1 second
                    val eeg = obj as? EEGPower
                    if (eeg != null && eeg.isValidate) {
                        val totalPower = (eeg.delta + eeg.theta + eeg.lowAlpha + eeg.highAlpha +
                                eeg.lowBeta + eeg.highBeta + eeg.lowGamma + eeg.middleGamma).toFloat()
                                .coerceAtLeast(1f)
                        val alphaSum = (eeg.lowAlpha + eeg.highAlpha).toFloat()
                        val betaSum = (eeg.lowBeta + eeg.highBeta).toFloat()

                        Log.d(TAG, "EEGPower: d=${eeg.delta} t=${eeg.theta} " +
                                "la=${eeg.lowAlpha} ha=${eeg.highAlpha} " +
                                "lb=${eeg.lowBeta} hb=${eeg.highBeta} " +
                                "lg=${eeg.lowGamma} mg=${eeg.middleGamma}")

                        _data.value = _data.value.copy(
                            delta = eeg.delta,
                            theta = eeg.theta,
                            lowAlpha = eeg.lowAlpha,
                            highAlpha = eeg.highAlpha,
                            lowBeta = eeg.lowBeta,
                            highBeta = eeg.highBeta,
                            lowGamma = eeg.lowGamma,
                            middleGamma = eeg.middleGamma,
                            alpha = alphaSum / totalPower,
                            beta = betaSum / totalPower,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                }
                MindDataType.CODE_RAW -> {
                    // Raw EEG at 512 Hz — for waveform plotting
                    rawBuffer[rawIndex] = data.toShort()
                    rawIndex++
                    if (rawIndex >= RAW_BUFFER_SIZE) {
                        // Feed to algo SDK for blink detection
                        try { NskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_EEG.value, rawBuffer, rawIndex) } catch (_: Exception) {}
                        _rawEeg.value = RawEegBuffer(
                            samples = rawBuffer.copyOf(),
                            index = RAW_BUFFER_SIZE,
                            timestamp = System.currentTimeMillis()
                        )
                        rawIndex = 0
                    }
                }
            }
        }
    }

    @Suppress("MissingPermission")
    fun findPairedDevices(): List<Pair<String, String>> {
        return bluetoothAdapter?.bondedDevices
            ?.filter { it.name?.contains("MindWave", ignoreCase = true) == true }
            ?.map { (it.name ?: "Unknown") to it.address }
            ?: emptyList()
    }

    @Suppress("MissingPermission")
    fun connect(deviceAddress: String) {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not available")
            _connectionState.value = ConnectionState.ERROR
            return
        }
        disconnect()
        try {
            val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)
            _connectionState.value = ConnectionState.CONNECTING
            tgStreamReader = TgStreamReader(device, streamCallback)
            tgStreamReader?.connect()
            Log.i(TAG, "Connecting to ${device.name} ($deviceAddress)...")
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed: ${e.message}")
            _connectionState.value = ConnectionState.ERROR
        }
    }

    fun disconnect() {
        try {
            tgStreamReader?.let {
                if (it.isBTConnected) { it.stop(); it.close() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error: ${e.message}")
        }
        tgStreamReader = null
        rawIndex = 0
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
}
