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
import com.neurosky.connection.TgStreamHandler
import com.neurosky.connection.TgStreamReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Bluetooth connection to NeuroSky MindWave Mobile 2.
 * Wraps TgStreamReader + NskAlgoSdk and exposes data via StateFlow.
 *
 * Usage:
 *   val conn = MindWaveConnection(context)
 *   conn.connect(deviceAddress)
 *   // observe conn.connectionState and conn.data
 *   conn.disconnect()
 */
class MindWaveConnection(context: Context) {

    companion object {
        private const val TAG = "MindWave"
        const val MINDWAVE_DEVICE_NAME = "MindWave Mobile"

        init {
            try {
                System.loadLibrary("NskAlgo")
                Log.i(TAG, "NskAlgo native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load NskAlgo native library: ${e.message}")
            }
        }
    }

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR
    }

    /** Raw data packet from the device */
    data class MindWaveData(
        val attention: Int = 0,          // 0–100
        val meditation: Int = 0,         // 0–100
        val signalQuality: Int = 200,    // 0=good, 200=no contact
        val blinkStrength: Int = 0,      // 0–255, event-based
        val blinkDetected: Boolean = false,
        // Band power (relative, from NskAlgoSdk)
        val delta: Float = 0f,
        val theta: Float = 0f,
        val alpha: Float = 0f,
        val beta: Float = 0f,
        val gamma: Float = 0f,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _data = MutableStateFlow(MindWaveData())
    val data: StateFlow<MindWaveData> = _data.asStateFlow()

    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private var tgStreamReader: TgStreamReader? = null
    private var nskAlgoSdk: NskAlgoSdk? = null
    private val rawBuffer = ShortArray(512)
    private var rawIndex = 0

    // Current values (updated from callbacks, merged into _data)
    private var curAttention = 0
    private var curMeditation = 0
    private var curSignalQuality = 200

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

        override fun onChecksumFail(payload: ByteArray?, length: Int, checksum: Int) {
            Log.e(TAG, "Checksum fail")
        }

        override fun onDataReceived(datatype: Int, data: Int, obj: Any?) {
            when (datatype) {
                MindDataType.CODE_ATTENTION -> {
                    curAttention = data
                    Log.d(TAG, "Attention: $data")
                    try { NskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ATT.value, shortArrayOf(data.toShort()), 1) } catch (e: Exception) { Log.e(TAG, "AlgoStream ATT error: ${e.message}") }
                    emitData()
                }
                MindDataType.CODE_MEDITATION -> {
                    curMeditation = data
                    Log.d(TAG, "Meditation: $data")
                    try { NskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_MED.value, shortArrayOf(data.toShort()), 1) } catch (e: Exception) { Log.e(TAG, "AlgoStream MED error: ${e.message}") }
                    emitData()
                }
                MindDataType.CODE_POOR_SIGNAL -> {
                    curSignalQuality = data
                    try { NskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_PQ.value, shortArrayOf(data.toShort()), 1) } catch (e: Exception) { Log.e(TAG, "AlgoStream PQ error: ${e.message}") }
                    emitData()
                }
                MindDataType.CODE_RAW -> {
                    rawBuffer[rawIndex++] = data.toShort()
                    if (rawIndex >= 512) {
                        Log.d(TAG, "Raw EEG batch: 512 samples")
                        try { NskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_EEG.value, rawBuffer, rawIndex) } catch (e: Exception) { Log.e(TAG, "AlgoStream EEG error: ${e.message}") }
                        rawIndex = 0
                    }
                }
                MindDataType.CODE_EEGPOWER -> {
                    // Direct EEG power data from TgStreamReader (no algo SDK needed)
                    Log.d(TAG, "EEG Power received")
                }
            }
        }
    }

    init {
        setupAlgoSdk()
    }

    private fun setupAlgoSdk() {
        val sdk = NskAlgoSdk()
        nskAlgoSdk = sdk

        sdk.setOnAttAlgoIndexListener { attentionValue ->
            Log.d(TAG, "AlgoSDK Attention: $attentionValue")
            curAttention = attentionValue
            emitData()
        }

        sdk.setOnBPAlgoIndexListener { delta, theta, alpha, beta, gamma ->
            Log.d(TAG, "AlgoSDK BandPower: d=$delta t=$theta a=$alpha b=$beta g=$gamma")
            _data.value = _data.value.copy(
                delta = delta, theta = theta, alpha = alpha,
                beta = beta, gamma = gamma,
                attention = curAttention, meditation = curMeditation,
                signalQuality = curSignalQuality,
                blinkDetected = false,
                timestamp = System.currentTimeMillis()
            )
        }

        sdk.setOnEyeBlinkDetectionListener { strength ->
            Log.d(TAG, "AlgoSDK Blink: $strength")
            _data.value = _data.value.copy(
                blinkStrength = strength,
                blinkDetected = true,
                attention = curAttention, meditation = curMeditation,
                signalQuality = curSignalQuality,
                timestamp = System.currentTimeMillis()
            )
        }

        sdk.setOnMedAlgoIndexListener { meditationValue ->
            Log.d(TAG, "AlgoSDK Meditation: $meditationValue")
            curMeditation = meditationValue
            emitData()
        }

        sdk.setOnSignalQualityListener { level ->
            Log.d(TAG, "AlgoSDK Signal quality level: $level")
        }

        // Initialize algorithms: attention + meditation + band power + blink detection
        val algoTypes = NskAlgoType.NSK_ALGO_TYPE_ATT.value or
                NskAlgoType.NSK_ALGO_TYPE_MED.value or
                NskAlgoType.NSK_ALGO_TYPE_BP.value or
                NskAlgoType.NSK_ALGO_TYPE_BLINK.value
        try {
            val initResult = NskAlgoSdk.NskAlgoInit(algoTypes, "")
            Log.i(TAG, "NskAlgoInit result: $initResult (types=0x${algoTypes.toString(16)})")
            val startResult = NskAlgoSdk.NskAlgoStart(false)
            Log.i(TAG, "NskAlgoStart result: $startResult")
        } catch (e: Exception) {
            Log.e(TAG, "NskAlgo init failed: ${e.message}", e)
        }
    }

    private fun emitData() {
        _data.value = _data.value.copy(
            attention = curAttention,
            meditation = curMeditation,
            signalQuality = curSignalQuality,
            blinkDetected = false,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Find paired MindWave devices.
     * Returns list of (name, MAC address) pairs.
     */
    @Suppress("MissingPermission")
    fun findPairedDevices(): List<Pair<String, String>> {
        return bluetoothAdapter?.bondedDevices
            ?.filter { it.name?.contains("MindWave", ignoreCase = true) == true }
            ?.map { (it.name ?: "Unknown") to it.address }
            ?: emptyList()
    }

    /**
     * Connect to a MindWave device by MAC address.
     */
    @Suppress("MissingPermission")
    fun connect(deviceAddress: String) {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not available")
            _connectionState.value = ConnectionState.ERROR
            return
        }

        disconnect() // clean up any existing connection

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

    /**
     * Disconnect and clean up.
     */
    fun disconnect() {
        try {
            tgStreamReader?.let {
                if (it.isBTConnected) {
                    it.stop()
                    it.close()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error: ${e.message}")
        }
        tgStreamReader = null
        rawIndex = 0
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    /** Check if Bluetooth is enabled */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
}
