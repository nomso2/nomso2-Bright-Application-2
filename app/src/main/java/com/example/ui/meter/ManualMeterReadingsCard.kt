package com.example.ui.meter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.meter.ManualMeterReading
import com.example.data.meter.ManualMeterReadings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** "Enter from your meter / vendor app": manual readings saved on this phone. */
@Composable
fun ManualMeterReadingsCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var readings by remember { mutableStateOf(ManualMeterReadings.all(context)) }
    var credit by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val fmt = remember { SimpleDateFormat("d MMM yyyy, h:mm a", Locale.UK) }

    Card(
        modifier = modifier.fillMaxWidth().testTag("manual_meter_readings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Meter readings: enter from your meter / vendor app", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            Text("There's no public meter server for consumers, so Bright doesn't invent readings. Type what your meter display or vendor app shows. Saved on this phone only.",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = credit, onValueChange = { credit = it.filter { c -> c.isDigit() || c == '.' }.take(10) },
                label = { Text("Credit left (kWh)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = total, onValueChange = { total = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
                label = { Text("Total used (kWh, optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = note, onValueChange = { note = it.take(80) },
                label = { Text("Note (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    ManualMeterReadings.add(context, ManualMeterReading(System.currentTimeMillis(), credit.toDoubleOrNull(), total.toDoubleOrNull(), note.trim()))
                    readings = ManualMeterReadings.all(context)
                    credit = ""; total = ""; note = ""
                },
                enabled = credit.toDoubleOrNull() != null || total.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth().testTag("manual_meter_reading_save")
            ) { Text("Save reading") }
            if (readings.isEmpty()) {
                Text("No readings yet.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                readings.take(10).forEach { r ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(listOfNotNull(
                                r.creditKwh?.let { "Credit ${"%.2f".format(it)} kWh" },
                                r.totalKwh?.let { "Total ${"%.1f".format(it)} kWh" }
                            ).joinToString(" · "), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(fmt.format(Date(r.takenAt)) + " · entered by you" + if (r.note.isNotBlank()) " · ${r.note}" else "",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { ManualMeterReadings.remove(context, r); readings = ManualMeterReadings.all(context) }) { Text("Remove") }
                    }
                }
            }
        }
    }
}
