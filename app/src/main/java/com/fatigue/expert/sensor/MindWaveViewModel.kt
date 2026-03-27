package com.fatigue.expert.sensor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel bridging MindWaveConnection + EegSignalProcessor with the UI.
 * Collects raw MindWave data, processes it into FIS inputs, and exposes
 * ready-to-use objective parameter maps.
 */
class MindWaveViewModel(application: Application) : AndroidViewModel(application) {

    val connection = MindWaveConnection(application)
    val processor = EegSignalProcessor()

    /** Processed FIS-ready inputs (0–2 scale) */
    val objectiveInputs: StateFlow<EegSignalProcessor.ObjectiveInputs> = processor.inputs

    /** Raw device data for debug display */
    val rawData: StateFlow<MindWaveConnection.MindWaveData> = connection.data

    /** Connection state */
    val connectionState: StateFlow<MindWaveConnection.ConnectionState> = connection.connectionState

    init {
        // Pipe raw data through the signal processor
        viewModelScope.launch {
            connection.data.collect { data ->
                processor.process(data)
            }
        }
    }

    /** Find paired MindWave devices */
    fun findPairedDevices(): List<Pair<String, String>> = connection.findPairedDevices()

    /** Connect to device by MAC address */
    fun connect(address: String) = connection.connect(address)

    /** Disconnect */
    fun disconnect() {
        connection.disconnect()
        processor.reset()
    }

    /** Get current inputs as a Map matching FIS variable names */
    fun getObjectiveInputMap(): Map<String, Float> {
        val i = objectiveInputs.value
        return mapOf(
            "mirkskinasanas_biezums" to i.blinkFrequency.toFloat(),
            "EEG_alfa_ritms" to i.eegAlpha.toFloat(),
            "EEG_j1" to i.j1.toFloat(),
            "EEG_j2" to i.j2.toFloat(),
            "EEG_j3" to i.j3.toFloat(),
            "EEG_j4" to i.j4.toFloat()
        )
    }

    /** Get alert-level inputs */
    fun getAlertInputMap(): Map<String, Float> {
        val i = objectiveInputs.value
        return mapOf(
            "EEG_vertejums" to i.eegAssessment.toFloat(),
            "Acu_novertejums" to i.eyeAssessment.toFloat()
        )
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
