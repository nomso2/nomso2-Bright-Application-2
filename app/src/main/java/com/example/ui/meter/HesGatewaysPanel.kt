package com.example.ui.meter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.meter.HesRegistry
import com.example.data.meter.MeterIdentity
import com.example.data.meter.MeterReading
import com.example.data.meter.MeterResult
import com.example.model.MeterManufacturer
import com.example.model.UserProfile
import kotlinx.coroutines.launch

/** HES / MDMS connections for the six manufacturers: real when configured, DEMO otherwise. */
@Composable
fun HesGatewaysPanel(userProfile: UserProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Each manufacturer's meters are reached through your DisCo's head-end system (HES / MDMS). No manufacturer offers a public consumer API. Add the endpoint and key your DisCo issues to connect for real; until then each card runs a DEMO meter with simulated values.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        MeterManufacturer.entries.forEach { m -> ManufacturerHesCard(m, userProfile) }
    }
}

@Composable
private fun ManufacturerHesCard(m: MeterManufacturer, userProfile: UserProfile) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val disco = userProfile.discoCode
    var configured by remember { mutableStateOf(HesRegistry.config(context, disco, m) != null) }
    var showConfig by remember { mutableStateOf(false) }
    var url by remember { mutableStateOf(HesRegistry.savedUrl(context, disco, m)) }
    var key by remember { mutableStateOf("") }
    var reading by remember { mutableStateOf<MeterReading?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val meterNumber = MeterIdentity.meterNumber(context, userProfile.meterNumber)

    MeterCard(m.displayName, badge = { if (configured) StatusChip("HES connected", ok = true) else DemoBadge("DEMO") }) {
        Text(if (configured) "Real HES for ${disco.ifBlank { "your DisCo" }}. Readings are live from the server."
             else "Needs DisCo HES access. Showing a DEMO meter (simulated).",
            fontSize = 13.sp, color = if (configured) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = !busy, onClick = {
                busy = true
                scope.launch {
                    when (val r = HesRegistry.connectorFor(context, disco, m).readTelemetry(meterNumber)) {
                        is MeterResult.Ok -> { reading = r.value; message = null }
                        is MeterResult.Fail -> message = r.message
                    }
                    busy = false
                }
            }, modifier = Modifier.testTag("hes_read_${m.name}")) { Text(if (configured) "Read telemetry" else "Connect DEMO meter") }
            OutlinedButton(enabled = !busy, onClick = {
                busy = true
                scope.launch {
                    val c = HesRegistry.connectorFor(context, disco, m)
                    message = when (val r = c.ping()) {
                        is MeterResult.Ok -> if (c.isDemo) "DEMO ping: ${r.value} ms (simulated latency)" else "App-to-server latency: ${r.value} ms"
                        is MeterResult.Fail -> r.message
                    }
                    busy = false
                }
            }) { Text("Ping") }
        }
        message?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary) }
        if (reading != null) TelemetryPanel(reading, title = if (reading!!.source.isDemo) "DEMO telemetry" else "Live telemetry")

        Text("No token buying or recharging here. Compensation tokens your DisCo owes for overdue fault repairs are listed under Compensation tokens.",
            fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        TextButton(onClick = { showConfig = !showConfig }) {
            Text(if (showConfig) "Hide HES settings" else if (configured) "HES settings" else "Add DisCo HES endpoint")
        }
        if (showConfig) {
            Text("For ${disco.ifBlank { "your DisCo" }} + ${m.displayName}. The key is encrypted with the phone's Keystore.",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(value = url, onValueChange = { url = it.trim() }, label = { Text("HES base URL (https://...)") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = key, onValueChange = { key = it.trim() },
                label = { Text(if (configured) "API key (saved; type to replace)" else "API key") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = url.startsWith("https://") && key.isNotBlank(), onClick = {
                    HesRegistry.save(context, disco, m, url, key)
                    configured = HesRegistry.config(context, disco, m) != null
                    key = ""
                    reading = null
                    message = if (configured) "Saved. This manufacturer now uses your DisCo's HES instead of DEMO." else "Couldn't save the key securely on this phone."
                }) { Text("Save") }
                if (configured) OutlinedButton(onClick = {
                    HesRegistry.remove(context, disco, m)
                    configured = false
                    reading = null
                    message = "Removed. Back to DEMO."
                }) { Text("Remove") }
            }
        }
    }
}
