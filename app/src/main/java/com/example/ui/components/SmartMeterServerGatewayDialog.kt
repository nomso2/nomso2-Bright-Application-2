package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.SmartMeterCommand
import com.example.model.SmartMeterDevice
import com.example.model.SmartMeterServerConfig
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkCanvas
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.MutedSlateText
import com.example.ui.theme.Slate100Text

enum class GatewayTab(val title: String) {
    METERS("Smart Meters"),
    SERVER_CONFIG("Server Config"),
    COMMANDS_LOG("Commands Log"),
    API_DOCS("API & Webhook Docs")
}

@Composable
fun SmartMeterServerGatewayDialog(
    serverConfig: SmartMeterServerConfig,
    metersList: List<SmartMeterDevice>,
    commandsHistory: List<SmartMeterCommand>,
    onUpdateServerConfig: (url: String, protocol: String, mqttBroker: String, apiKey: String, interval: Int, tls: Boolean) -> Unit,
    onTestServerConnection: () -> Unit,
    onAddMeterDevice: (number: String, manufacturer: String, model: String, disco: String, state: String, feeder: String, ip: String, protocol: String) -> Unit,
    onToggleRelay: (meterNumber: String) -> Unit,
    onSendOtaToken: (meterNumber: String, token: String) -> Unit,
    onPingMeter: (meterNumber: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(GatewayTab.METERS) }

    // Dialog states
    var showAddMeterModal by remember { mutableStateOf(false) }
    var tokenTargetMeter by remember { mutableStateOf<SmartMeterDevice?>(null) }
    var otaTokenInput by remember { mutableStateOf("") }

    // Config form states initialized from serverConfig
    var formServerUrl by remember(serverConfig) { mutableStateOf(serverConfig.serverUrl) }
    var formProtocol by remember(serverConfig) { mutableStateOf(serverConfig.protocol) }
    var formMqttHost by remember(serverConfig) { mutableStateOf(serverConfig.mqttBrokerHost) }
    var formApiKey by remember(serverConfig) { mutableStateOf(serverConfig.apiKey) }
    var formSyncInterval by remember(serverConfig) { mutableIntStateOf(serverConfig.syncIntervalSeconds) }
    var formTlsEnabled by remember(serverConfig) { mutableStateOf(serverConfig.tlsEnabled) }

    // Meter filter by DisCo
    var selectedDiscoFilter by remember { mutableStateOf("ALL") }

    val filteredMeters = remember(metersList, selectedDiscoFilter) {
        if (selectedDiscoFilter == "ALL") metersList
        else metersList.filter { it.discoCode.equals(selectedDiscoFilter, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE60A0C10))
                .padding(horizontal = 8.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.96f)
                    .testTag("smart_meter_server_gateway_dialog"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkCanvas),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElegantGoldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Router,
                                    contentDescription = null,
                                    tint = ElegantGoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "NIGERIA SMART METER SERVER GATEWAY",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.White
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (serverConfig.isConnected) Color(0xFF22C55E) else Color(0xFFEF4444)
                                            )
                                    )
                                    Text(
                                        text = if (serverConfig.isConnected) "SERVER CONNECTED (${serverConfig.latencyMs}ms)" else "OFFLINE / DISCONNECTED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (serverConfig.isConnected) Color(0xFF22C55E) else Color(0xFFEF4444)
                                    )
                                    Text(
                                        text = "• ${serverConfig.protocol}",
                                        fontSize = 11.sp,
                                        color = MutedSlateText
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                                .testTag("close_smart_meter_gateway_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Navigation Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = Color(0xFF11141B),
                        contentColor = ElegantGoldPrimary,
                        edgePadding = 12.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                color = ElegantGoldPrimary
                            )
                        }
                    ) {
                        GatewayTab.values().forEach { tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                text = {
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == tab) ElegantGoldPrimary else MutedSlateText,
                                        fontSize = 13.sp
                                    )
                                }
                            )
                        }
                    }

                    // Tab Content Body
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            GatewayTab.METERS -> {
                                MetersCatalogTab(
                                    meters = filteredMeters,
                                    selectedDisco = selectedDiscoFilter,
                                    onSelectDisco = { selectedDiscoFilter = it },
                                    onAddNewMeter = { showAddMeterModal = true },
                                    onToggleRelay = onToggleRelay,
                                    onOpenOtaTokenDialog = { meter ->
                                        tokenTargetMeter = meter
                                        otaTokenInput = ""
                                    },
                                    onPingMeter = onPingMeter
                                )
                            }
                            GatewayTab.SERVER_CONFIG -> {
                                ServerConfigTab(
                                    serverUrl = formServerUrl,
                                    onServerUrlChange = { formServerUrl = it },
                                    protocol = formProtocol,
                                    onProtocolChange = { formProtocol = it },
                                    mqttHost = formMqttHost,
                                    onMqttHostChange = { formMqttHost = it },
                                    apiKey = formApiKey,
                                    onApiKeyChange = { formApiKey = it },
                                    syncInterval = formSyncInterval,
                                    onSyncIntervalChange = { formSyncInterval = it },
                                    tlsEnabled = formTlsEnabled,
                                    onTlsToggle = { formTlsEnabled = it },
                                    serverConfig = serverConfig,
                                    onTestConnection = onTestServerConnection,
                                    onSave = {
                                        onUpdateServerConfig(
                                            formServerUrl,
                                            formProtocol,
                                            formMqttHost,
                                            formApiKey,
                                            formSyncInterval,
                                            formTlsEnabled
                                        )
                                    }
                                )
                            }
                            GatewayTab.COMMANDS_LOG -> {
                                CommandsLogTab(commands = commandsHistory)
                            }
                            GatewayTab.API_DOCS -> {
                                ApiIntegrationDocsTab(serverConfig = serverConfig)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Register / Connect New Smart Meter
    if (showAddMeterModal) {
        AddSmartMeterModal(
            onDismiss = { showAddMeterModal = false },
            onAdd = { num, mfg, model, disco, state, feeder, ip, proto ->
                onAddMeterDevice(num, mfg, model, disco, state, feeder, ip, proto)
                showAddMeterModal = false
            }
        )
    }

    // Modal: Over-The-Air (OTA) STS Token Injection
    tokenTargetMeter?.let { meter ->
        AlertDialog(
            onDismissRequest = { tokenTargetMeter = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = ElegantGoldPrimary)
                    Text("Over-The-Air Token Push", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Transmit 20-digit STS prepayment token over cellular/APN bridge directly to Meter #${meter.meterNumber} (${meter.manufacturer}). No physical keypad entry required.",
                        fontSize = 12.sp,
                        color = MutedSlateText
                    )
                    OutlinedTextField(
                        value = otaTokenInput,
                        onValueChange = { if (it.length <= 24) otaTokenInput = it },
                        label = { Text("20-Digit STS Token") },
                        placeholder = { Text("4829 1092 3841 9201 3819") },
                        modifier = Modifier.fillMaxWidth().testTag("ota_token_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantGoldPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (otaTokenInput.isNotBlank()) {
                            onSendOtaToken(meter.meterNumber, otaTokenInput)
                            tokenTargetMeter = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantGoldPrimary, contentColor = Color.Black),
                    modifier = Modifier.testTag("submit_ota_token_btn")
                ) {
                    Text("Transmit OTA Token", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { tokenTargetMeter = null }) {
                    Text("Cancel", color = MutedSlateText)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

// -----------------------------------------------------------------------------
// TAB 1: METERS CATALOG
// -----------------------------------------------------------------------------
@Composable
private fun MetersCatalogTab(
    meters: List<SmartMeterDevice>,
    selectedDisco: String,
    onSelectDisco: (String) -> Unit,
    onAddNewMeter: () -> Unit,
    onToggleRelay: (String) -> Unit,
    onOpenOtaTokenDialog: (SmartMeterDevice) -> Unit,
    onPingMeter: (String) -> Unit
) {
    val discos = listOf("ALL", "EKEDC", "IKEDC", "AEDC", "IBEDC", "EEDC", "KEDCO")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Top Overview Banner & Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CONNECTED SMART METERS IN NIGERIA",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElegantGoldPrimary
                )
                Text(
                    text = "${meters.count { it.isOnline }} Online / ${meters.size} Registered Meters",
                    fontSize = 12.sp,
                    color = MutedSlateText
                )
            }

            Button(
                onClick = onAddNewMeter,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantGoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_new_smart_meter_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Connect Meter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // DisCo Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(discos) { disco ->
                val isSelected = selectedDisco == disco
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) ElegantGoldPrimary else Color(0xFF1E293B)
                        )
                        .clickable { onSelectDisco(disco) }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = disco,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else Slate100Text
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Meters List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(meters, key = { it.meterNumber }) { meter ->
                SmartMeterDeviceCard(
                    meter = meter,
                    onToggleRelay = { onToggleRelay(meter.meterNumber) },
                    onOpenOtaToken = { onOpenOtaTokenDialog(meter) },
                    onPing = { onPingMeter(meter.meterNumber) }
                )
            }
        }
    }
}

@Composable
private fun SmartMeterDeviceCard(
    meter: SmartMeterDevice,
    onToggleRelay: () -> Unit,
    onOpenOtaToken: () -> Unit,
    onPing: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("smart_meter_card_${meter.meterNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161920)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (meter.tamperDetected) Color(0xFFEF4444).copy(alpha = 0.6f) else ElegantDarkBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Meter number, DisCo & Online Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (meter.isOnline) Color(0xFF22C55E) else Color(0xFFEF4444))
                    )
                    Text(
                        text = "METER #${meter.meterNumber}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${meter.discoCode} • ${meter.locationState}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantGoldPrimary
                        )
                    }

                    if (meter.tamperDetected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TAMPER ALERT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle: Manufacturer & Model
            Text(
                text = "${meter.manufacturer} (${meter.modelNumber}) • ${meter.protocol}",
                fontSize = 11.sp,
                color = MutedSlateText
            )
            Text(
                text = "Feeder: ${meter.feederName} | APN: ${meter.ipOrSimImei}",
                fontSize = 10.sp,
                color = MutedSlateText
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Real-Time Electrical Telemetry Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TelemetryMetricItem(
                    label = "VOLTAGE",
                    value = "${meter.voltageV} V",
                    color = if (meter.voltageV > 210.0) Color(0xFF22C55E) else Color(0xFFEF4444)
                )
                TelemetryMetricItem(
                    label = "CURRENT",
                    value = "${meter.currentA} A",
                    color = Color(0xFF60A5FA)
                )
                TelemetryMetricItem(
                    label = "FREQUENCY",
                    value = "${meter.frequencyHz} Hz",
                    color = Color(0xFFEAB308)
                )
                TelemetryMetricItem(
                    label = "ACTIVE LOAD",
                    value = "${meter.activePowerKw} kW",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Relay Status and Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (meter.relayStatusClosed) Icons.Default.Power else Icons.Default.PowerOff,
                        contentDescription = null,
                        tint = if (meter.relayStatusClosed) Color(0xFF22C55E) else Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (meter.relayStatusClosed) "Relay Closed (Supply ON)" else "Relay Tripped (Supply CUT)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (meter.relayStatusClosed) Color(0xFF22C55E) else Color(0xFFEF4444)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Poll Telemetry
                    IconButton(
                        onClick = onPing,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("ping_meter_btn_${meter.meterNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Poll Instant Read",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // OTA Token Vending
                    IconButton(
                        onClick = onOpenOtaToken,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("ota_token_btn_${meter.meterNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Push OTA Token",
                            tint = ElegantGoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Toggle Relay Switch
                    Button(
                        onClick = onToggleRelay,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (meter.relayStatusClosed) Color(0xFF991B1B) else Color(0xFF15803D),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("relay_switch_btn_${meter.meterNumber}")
                    ) {
                        Text(
                            text = if (meter.relayStatusClosed) "Cut Supply" else "Restore",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryMetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = MutedSlateText, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 12.sp, color = color, fontWeight = FontWeight.ExtraBold)
    }
}

// -----------------------------------------------------------------------------
// TAB 2: SERVER CONFIGURATION
// -----------------------------------------------------------------------------
@Composable
private fun ServerConfigTab(
    serverUrl: String,
    onServerUrlChange: (String) -> Unit,
    protocol: String,
    onProtocolChange: (String) -> Unit,
    mqttHost: String,
    onMqttHostChange: (String) -> Unit,
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    syncInterval: Int,
    onSyncIntervalChange: (Int) -> Unit,
    tlsEnabled: Boolean,
    onTlsToggle: (Boolean) -> Unit,
    serverConfig: SmartMeterServerConfig,
    onTestConnection: () -> Unit,
    onSave: () -> Unit
) {
    val protocols = listOf(
        "REST_MQTT_HYBRID" to "REST API & MQTT Telemetry Stream",
        "DLMS_COSEM_HDLC" to "DLMS / COSEM Gateway (IEC 62056)",
        "STS6_CELLULAR_APN" to "STS-6 Direct Cellular APN (Private DisCo VPN)",
        "WEBSOCKET_STREAM" to "WebSocket Real-Time Bi-Directional Stream"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Live Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161920)),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "APP SERVER LINK STATUS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElegantGoldPrimary
                        )
                        Text(
                            text = if (serverConfig.isConnected) "Online (Roundtrip: ${serverConfig.latencyMs}ms)" else "Disconnected / Unreachable",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (serverConfig.isConnected) Color(0xFF22C55E) else Color(0xFFEF4444)
                        )
                        Text(
                            text = "Last Handshake: ${serverConfig.lastHeartbeatTime}",
                            fontSize = 11.sp,
                            color = MutedSlateText
                        )
                    }

                    Button(
                        onClick = onTestConnection,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("test_server_ping_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Ping", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Primary App Server Base URL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100Text
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = serverUrl,
                onValueChange = onServerUrlChange,
                placeholder = { Text("https://ami.brightgrid.ng/api/v1") },
                modifier = Modifier.fillMaxWidth().testTag("server_url_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantGoldPrimary,
                    unfocusedBorderColor = ElegantDarkBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        item {
            Text(
                text = "Communication Protocol",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100Text
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                protocols.forEach { (code, label) ->
                    val isSelected = protocol == code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A))
                            .border(
                                1.dp,
                                if (isSelected) ElegantGoldPrimary else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onProtocolChange(code) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ElegantGoldPrimary else Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Black))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = code, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = label, fontSize = 10.sp, color = MutedSlateText)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "MQTT IoT Broker Host:Port",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100Text
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = mqttHost,
                onValueChange = onMqttHostChange,
                placeholder = { Text("mqtt.brightgrid.ng:8883") },
                modifier = Modifier.fillMaxWidth().testTag("mqtt_host_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantGoldPrimary,
                    unfocusedBorderColor = ElegantDarkBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        item {
            Text(
                text = "Server API Bearer Key / Secret Token",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100Text
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                placeholder = { Text("ami_live_ng_sec_...") },
                modifier = Modifier.fillMaxWidth().testTag("api_key_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantGoldPrimary,
                    unfocusedBorderColor = ElegantDarkBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Enforce TLS / SSL mTLS Verification",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100Text
                    )
                    Text(
                        text = "Requires valid CA certificate for meter communication",
                        fontSize = 10.sp,
                        color = MutedSlateText
                    )
                }
                Switch(
                    checked = tlsEnabled,
                    onCheckedChange = onTlsToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = ElegantGoldPrimary)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().testTag("save_server_config_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantGoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save & Apply Gateway Configuration", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 3: COMMANDS AUDIT LOG
// -----------------------------------------------------------------------------
@Composable
private fun CommandsLogTab(commands: List<SmartMeterCommand>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "REMOTE DISPATCH AUDIT LOG",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = ElegantGoldPrimary
        )
        Text(
            text = "Cryptographically timestamped commands sent to Nigerian smart meters",
            fontSize = 11.sp,
            color = MutedSlateText
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (commands.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No remote commands dispatched yet.", color = MutedSlateText)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(commands) { cmd ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161920)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = cmd.commandType,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = cmd.status, fontSize = 9.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = "Meter #${cmd.meterNumber} • Payload: ${cmd.payload}",
                                    fontSize = 11.sp,
                                    color = MutedSlateText
                                )
                            }
                            Text(text = cmd.timestampText, fontSize = 10.sp, color = MutedSlateText)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 4: API & WEBHOOK DOCS
// -----------------------------------------------------------------------------
@Composable
private fun ApiIntegrationDocsTab(serverConfig: SmartMeterServerConfig) {
    val context = LocalContext.current

    val curlExample = """
curl -X POST "${serverConfig.serverUrl}/telemetry/push" \
  -H "Authorization: Bearer ${serverConfig.apiKey}" \
  -H "Content-Type: application/json" \
  -d '{
    "meterNumber": "01429583192",
    "voltageV": 229.4,
    "currentA": 11.2,
    "frequencyHz": 50.01,
    "activePowerKw": 2.56,
    "accumulatedKwh": 1422.3,
    "relayStatus": "CLOSED",
    "tamper": false,
    "discoCode": "EKEDC"
  }'
    """.trimIndent()

    val nodeJsExample = """
// Node.js (Express / Fastify) Smart Meter Ingestion Server
const express = require('express');
const app = express();
app.use(express.json());

// Ingest live DLMS/COSEM or STS telemetry from Nigerian smart meters
app.post('/api/v1/telemetry/push', (req, res) => {
  const { meterNumber, voltageV, currentA, relayStatus, tamper } = req.body;
  console.log(`[AMI STREAM] Meter ${'$'}{meterNumber}: ${'$'}{voltageV}V, ${'$'}{currentA}A`);
  
  // Trigger instant Last-Gasp alert if voltage drops to zero
  if (voltageV === 0) {
    notifyOutageSubscribers(meterNumber);
  }
  
  res.status(200).json({ status: 'ACKNOWLEDGED', timestamp: Date.now() });
});

app.listen(8080, () => console.log('Smart Meter Gateway running on port 8080'));
    """.trimIndent()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "DEVELOPER & SERVER INTEGRATION ARCHITECTURE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = ElegantGoldPrimary
            )
            Text(
                text = "Connect your backend server to receive live telemetry from Mojec, Momas, Conlog, and Hexing smart meters across Nigerian DisCos.",
                fontSize = 11.sp,
                color = MutedSlateText
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161920)),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "cURL Telemetry Push Sample", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("cURL Sample", curlExample))
                                Toast.makeText(context, "Copied cURL command!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = ElegantGoldPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = curlExample,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161920)),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Node.js Server Endpoint", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Node.js Sample", nodeJsExample))
                                Toast.makeText(context, "Copied Node.js code!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = ElegantGoldPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = nodeJsExample,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF4ADE80),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// MODAL: ADD NEW SMART METER
// -----------------------------------------------------------------------------
@Composable
private fun AddSmartMeterModal(
    onDismiss: () -> Unit,
    onAdd: (num: String, mfg: String, model: String, disco: String, state: String, feeder: String, ip: String, proto: String) -> Unit
) {
    var meterNumber by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("Mojec International") }
    var modelNumber by remember { mutableStateOf("M100-3P Smart") }
    var discoCode by remember { mutableStateOf("EKEDC") }
    var locationState by remember { mutableStateOf("Lagos") }
    var feederName by remember { mutableStateOf("Victoria Island 33kV Feeder") }
    var ipOrSim by remember { mutableStateOf("10.142.88.50 (MTN APN)") }
    var protocol by remember { mutableStateOf("DLMS/COSEM HDLC") }

    val manufacturers = listOf(
        "Mojec International",
        "Momas (MEMCOL)",
        "Conlog Nigeria",
        "Hexing Electrical",
        "Inhemeter",
        "Holley Metering"
    )

    val discos = listOf("EKEDC", "IKEDC", "AEDC", "IBEDC", "EEDC", "KEDCO", "PHED")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Router, contentDescription = null, tint = ElegantGoldPrimary)
                Text("Connect New Smart Meter", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Register a Nigerian smart meter into the AMI Head-End server gateway.",
                        fontSize = 11.sp,
                        color = MutedSlateText
                    )
                }

                item {
                    OutlinedTextField(
                        value = meterNumber,
                        onValueChange = { meterNumber = it },
                        label = { Text("Smart Meter Number (11 or 13 digits)") },
                        placeholder = { Text("01429583192") },
                        modifier = Modifier.fillMaxWidth().testTag("modal_meter_number_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantGoldPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    Text("Manufacturer Brand", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate100Text)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(manufacturers) { mfg ->
                            val isSel = manufacturer == mfg
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ElegantGoldPrimary else Color(0xFF0F172A))
                                    .clickable { manufacturer = mfg }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = mfg,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Assigned DisCo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate100Text)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(discos) { d ->
                            val isSel = discoCode == d
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ElegantGoldPrimary else Color(0xFF0F172A))
                                    .clickable { discoCode = d }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = d,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = feederName,
                        onValueChange = { feederName = it },
                        label = { Text("Feeder / Injection Substation") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantGoldPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = ipOrSim,
                        onValueChange = { ipOrSim = it },
                        label = { Text("Cellular APN IP or SIM IMEI") },
                        placeholder = { Text("10.142.88.50 (MTN APN)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantGoldPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(meterNumber, manufacturer, modelNumber, discoCode, locationState, feederName, ipOrSim, protocol)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantGoldPrimary,
                    contentColor = Color.Black
                ),
                modifier = Modifier.testTag("confirm_add_smart_meter_btn")
            ) {
                Text("Connect Meter", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedSlateText)
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}
