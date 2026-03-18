package com.fatigue.expert.ui.screens.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fatigue.expert.ui.BiString
import com.fatigue.expert.ui.Language
import com.fatigue.expert.ui.currentLanguage

private data class SensorInfo(
    val name: String,
    val connectivity: BiString,
    val electrodes: BiString,
    val signals: BiString,
    val description: BiString
)

private val sensors = listOf(
    SensorInfo(
        "NeuroSky MindWave Mobile 2",
        BiString("Bluetooth 4.0 BLE", "Bluetooth 4.0 BLE"),
        BiString("1 sausais elektrods (pieres FP1)", "1 dry electrode (forehead FP1)"),
        BiString(
            "EEG neapstrādāts signāls (512 Hz), uzmanības un meditācijas līmenis, acu mirkšķināšana, " +
            "spektrālās jaudas joslas (delta, theta, alfa, beta, gamma)",
            "Raw EEG signal (512 Hz), attention and meditation levels, eye blink detection, " +
            "spectral power bands (delta, theta, alpha, beta, gamma)"
        ),
        BiString(
            "Kompakts un viegli lietojams patērētāju līmeņa EEG sensors. " +
            "Piemērots pētniecībai un izglītībai. Nodrošina pamata EEG ritmu analīzi un acu mirkšķināšanas noteikšanu.",
            "Compact and easy-to-use consumer-grade EEG sensor. " +
            "Suitable for research and education. Provides basic EEG rhythm analysis and blink detection."
        )
    ),
    SensorInfo(
        "MyndPlay MyndBand",
        BiString("Bluetooth 2.0 + EDR", "Bluetooth 2.0 + EDR"),
        BiString("1 sausais elektrods (pieres)", "1 dry electrode (forehead)"),
        BiString(
            "EEG neapstrādāts signāls, uzmanības un atslābināšanās līmeņi, " +
            "spektrālās joslas (delta, theta, low-alpha, high-alpha, low-beta, high-beta, gamma)",
            "Raw EEG signal, attention and relaxation levels, " +
            "spectral bands (delta, theta, low-alpha, high-alpha, low-beta, high-beta, gamma)"
        ),
        BiString(
            "Galvas lentes formas EEG sensors ar NeuroSky TGAM čipu. " +
            "Ērts ilgstošai valkāšanai. Izmantojams spēlēs, meditācijā un kognitīvo stāvokļu monitoringā.",
            "Headband-form EEG sensor with NeuroSky TGAM chip. " +
            "Comfortable for extended wear. Used in gaming, meditation and cognitive state monitoring."
        )
    ),
    SensorInfo(
        "BrainAccess HALO",
        BiString("Bluetooth 5.0 BLE", "Bluetooth 5.0 BLE"),
        BiString("8 sausi elektrodi (konfigurējami)", "8 dry electrodes (configurable)"),
        BiString(
            "Daudzkanālu EEG (250 Hz), impedances mērīšana, akselerometrs, " +
            "pilna spektra analīze (delta, theta, alfa, beta), ERP komponentes, " +
            "reāllaika datu straumēšana",
            "Multi-channel EEG (250 Hz), impedance measurement, accelerometer, " +
            "full spectrum analysis (delta, theta, alpha, beta), ERP components, " +
            "real-time data streaming"
        ),
        BiString(
            "Profesionāla līmeņa daudzkanālu EEG sistēma ar sausajiem elektrodiem. " +
            "Nodrošina augstas kvalitātes signālu pētniecībai un klīniskai lietošanai. " +
            "Atbalsta pielāgojamu elektrodu izvietojumu un reāllaika datu apstrādi.",
            "Professional-grade multi-channel EEG system with dry electrodes. " +
            "Provides high-quality signals for research and clinical use. " +
            "Supports configurable electrode placement and real-time data processing."
        )
    ),
    SensorInfo(
        BiString("Prototips", "Prototype").get(),
        BiString("USB / seriālais savienojums", "USB / serial connection"),
        BiString("Pielāgojams (1–4 elektrodi)", "Configurable (1–4 electrodes)"),
        BiString(
            "EEG neapstrādāts signāls, acu mirkšķināšanas noteikšana, " +
            "EMG signāls, pulsa frekvence (atkarībā no konfigurācijas)",
            "Raw EEG signal, blink detection, " +
            "EMG signal, heart rate (depending on configuration)"
        ),
        BiString(
            "Pētniecības prototips ar BITalino vai līdzīgu platformu. " +
            "Pilnībā pielāgojams sensoru un signālu apstrādes konfigurācijai. " +
            "Izmantots laboratorijas eksperimentos sistēmas izstrādes laikā.",
            "Research prototype based on BITalino or similar platform. " +
            "Fully configurable for sensor and signal processing setup. " +
            "Used in laboratory experiments during system development."
        )
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var selectedSensor by remember { mutableStateOf<Int?>(null) }
    var showSensorInfo by remember { mutableStateOf<SensorInfo?>(null) }

    // Sensor info dialog
    if (showSensorInfo != null) {
        val info = showSensorInfo!!
        AlertDialog(
            onDismissRequest = { showSensorInfo = null },
            title = { Text(info.name, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(info.description.get(), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    DetailRow(Icons.Default.Bluetooth, BiString("Savienojums", "Connectivity").get(), info.connectivity.get())
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.Sensors, BiString("Elektrodi", "Electrodes").get(), info.electrodes.get())
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.GraphicEq, BiString("Signāli", "Signals").get(), info.signals.get())
                }
            },
            confirmButton = {
                TextButton(onClick = { showSensorInfo = null }) {
                    Text(BiString("Aizvērt", "Close").get())
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(BiString("Iestatījumi", "Settings").get()) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ── Language ──
            Text(BiString("Valoda", "Language").get(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val isLV = currentLanguage.value == Language.LV
                if (isLV) Button(onClick = {}) { Text("🇱🇻 Latviešu") }
                else OutlinedButton(onClick = { currentLanguage.value = Language.LV }) { Text("🇱🇻 Latviešu") }
                if (!isLV) Button(onClick = {}) { Text("🇬🇧 English") }
                else OutlinedButton(onClick = { currentLanguage.value = Language.EN }) { Text("🇬🇧 English") }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Sensor selection ──
            Text(BiString("Sensora izvēle", "Sensor Selection").get(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                BiString("Izvēlieties EEG sensoru monitoringa funkcijai", "Select EEG sensor for monitoring function").get(),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // None option
            SensorOption(
                name = BiString("Nav izvēlēts", "None selected").get(),
                isSelected = selectedSensor == null,
                onSelect = { selectedSensor = null },
                onInfo = null
            )

            sensors.forEachIndexed { index, sensor ->
                SensorOption(
                    name = sensor.name,
                    isSelected = selectedSensor == index,
                    onSelect = { selectedSensor = index },
                    onInfo = { showSensorInfo = sensor }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Actuator selection ──
            Text(BiString("Aktuatora izvēle", "Actuator Selection").get(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                BiString("Trauksmes signālu izvades ierīce", "Alert signal output device").get(),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            var showActuatorInfo by remember { mutableStateOf(false) }

            if (showActuatorInfo) {
                AlertDialog(
                    onDismissRequest = { showActuatorInfo = false },
                    title = { Text(BiString("Aparatūras prototips", "Hardware Prototype").get(), fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(
                                BiString(
                                    "Trauksmes aktuatoru prototips ir izstrādāts kā valkājamas iekārtas papildinājums, " +
                                    "kas integrējas ar monitoringa sistēmu un nodrošina tiešu fizisku brīdinājumu lietotājam.",
                                    "The alert actuator prototype is developed as a wearable equipment add-on, " +
                                    "integrating with the monitoring system to provide direct physical warnings to the user."
                                ).get(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            DetailRow(Icons.Default.VolumeUp, BiString("Zummers", "Buzzer").get(),
                                BiString("Zemas intensitātes skaņas signāls vieglas miegainības gadījumā", "Low-intensity sound signal for mild drowsiness").get())
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow(Icons.Default.NotificationsActive, BiString("Zvans", "Bell").get(),
                                BiString("Vidējas intensitātes skaņas signāls pieaugošas miegainības gadījumā", "Medium-intensity sound signal for increasing drowsiness").get())
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow(Icons.Default.Warning, BiString("Sirēna un gaisma", "Siren and light").get(),
                                BiString("Augstas intensitātes skaņas un gaismas signāls kritiskas miegainības gadījumā", "High-intensity sound and light signal for critical drowsiness").get())
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow(Icons.Default.Bluetooth, BiString("Savienojums", "Connectivity").get(),
                                BiString("Bluetooth / MQTT savienojums ar monitoringa sistēmu", "Bluetooth / MQTT connection with monitoring system").get())
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow(Icons.Default.Memory, BiString("Platforma", "Platform").get(),
                                BiString("Raspberry Pi / Arduino bāzēts kontrolieris ar skaņas un gaismas moduļiem", "Raspberry Pi / Arduino based controller with sound and light modules").get())
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showActuatorInfo = false }) {
                            Text(BiString("Aizvērt", "Close").get())
                        }
                    }
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DoNotDisturb, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(BiString("Nav izvēlēts", "None selected").get(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(
                            BiString("Aktuatoru integrācija nav pieejama šajā versijā", "Actuator integration not available in this version").get(),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = {}, enabled = false) {
                    Icon(Icons.Default.Hardware, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(BiString("Aparatūras prototips", "Hardware Prototype").get())
                }
                IconButton(onClick = { showActuatorInfo = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                OutlinedButton(onClick = {}, enabled = false) {
                    Text(BiString("Nav", "None").get())
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── About ──
            Text(BiString("Par sistēmu", "About").get(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        BiString(
                            "Modulāra ekspertu datorsistēma cilvēka noguruma un miegainības novērtēšanai",
                            "Modular expert computer system for human fatigue and drowsiness assessment"
                        ).get(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("v1.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("© RTU / VDI", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SensorOption(
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onInfo: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        colors = if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onSelect)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (onInfo != null) {
                IconButton(onClick = onInfo, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.bodySmall)
        }
    }
}
