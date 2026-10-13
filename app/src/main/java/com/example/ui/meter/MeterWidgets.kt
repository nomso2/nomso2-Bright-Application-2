package com.example.ui.meter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.meter.MeterReading
import com.example.data.meter.ReadingSource
import com.example.data.meter.RelayStatus
import com.example.data.meter.TamperFlag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Loud "DEMO" label. Shown on every screen area that displays simulated values. */
@Composable
fun DemoBadge(text: String = "DEMO") {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.18f))
            .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("demo_badge")
    ) {
        Text(text, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
    }
}

@Composable
fun StatusChip(text: String, ok: Boolean) {
    val c = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(c.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) { Text(text, color = c, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
}

@Composable
fun MeterCard(title: String, badge: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f))
                badge?.invoke()
            }
            content()
        }
    }
}

@Composable
fun KeyValue(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

/**
 * Telemetry / relay / tamper panel. Shows only values a real source (optical probe or HES) or the
 * DEMO meter reported; with no reading it says "Not connected".
 */
@Composable
fun TelemetryPanel(reading: MeterReading?, title: String = "Meter telemetry") {
    MeterCard(title, badge = {
        when {
            reading == null -> StatusChip("Not connected", ok = false)
            reading.source.isDemo -> DemoBadge()
            else -> StatusChip(reading.source.label, ok = true)
        }
    }) {
        if (reading == null) {
            Text("Not connected. Read the meter with an optical probe, or connect your DisCo's HES. The DEMO meter shows how it works with simulated values.",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@MeterCard
        }
        if (reading.source.isDemo) {
            Text("DEMO: simulated values, not your meter.", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        val fmt = SimpleDateFormat("d MMM, h:mm:ss a", Locale.UK)
        KeyValue("Source", reading.source.label)
        KeyValue("Read at", fmt.format(Date(reading.takenAt)))
        reading.meterIdentification?.let { KeyValue("Meter says", it) }
        fun n(v: Double?, unit: String, places: Int = 2) = v?.let { "%.${places}f %s".format(it, unit) } ?: "Not reported"
        KeyValue("Credit left", n(reading.creditKwh, "kWh"))
        KeyValue("Total imported", n(reading.totalImportKwh, "kWh", 1))
        reading.totalExportKwh?.let { KeyValue("Total exported", n(it, "kWh", 1)) }
        KeyValue("Load", reading.activePowerW?.let { if (it >= 1000) "%.2f kW".format(it / 1000) else "%.0f W".format(it) } ?: "Not reported")
        KeyValue("Voltage", n(reading.voltageV, "V", 1))
        KeyValue("Current", n(reading.currentA, "A"))
        KeyValue("Power factor", reading.powerFactor?.let { "%.2f".format(it) } ?: "Not reported")
        reading.frequencyHz?.let { KeyValue("Frequency", n(it, "Hz")) }
        KeyValue("Relay", reading.relay.label,
            if (reading.relay == RelayStatus.CONNECTED) MaterialTheme.colorScheme.primary
            else if (reading.relay == RelayStatus.UNKNOWN) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.error)
        if (!reading.tamperReported) {
            KeyValue("Tamper / fault flags", "Not reported")
        } else if (reading.tamperFlags.isEmpty()) {
            KeyValue("Tamper / fault flags", "None")
        } else {
            Text("Tamper / fault flags", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TamperFlag.entries.filter { it in reading.tamperFlags }.forEach {
                Text("• ${it.label}", color = MaterialTheme.colorScheme.error, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (reading.source == ReadingSource.OPTICAL && reading.rawLines.isNotEmpty()) {
            Text("Raw readout (${reading.rawLines.size} lines)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            reading.rawLines.take(40).forEach { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}
