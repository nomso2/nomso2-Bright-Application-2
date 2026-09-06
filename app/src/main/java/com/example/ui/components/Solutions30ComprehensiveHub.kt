package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DisCo
import com.example.model.PowerProblemSolution
import com.example.model.PowerSector30Registry
import com.example.model.SolutionCategory
import com.example.model.UserProfile
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary

@Composable
fun Solutions30ComprehensiveHub(
    userProfile: UserProfile,
    isBatSignalMode: Boolean,
    onToggleBatSignalMode: (Boolean) -> Unit,
    onOpenForum: () -> Unit,
    onOpenRedDangerSOS: () -> Unit,
    onPlaySirenAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<SolutionCategory?>(null) }
    var activeModalSolutionNumber by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    // Interactive state across solutions
    var selectedUrgencyTier by remember { mutableStateOf(1) } // Solution 1
    var simulatedDiagnosticMode by remember { mutableStateOf("LOAD_SHEDDING") } // Solution 5
    var estimatedBillInput by remember { mutableStateOf("45000") } // Solution 13
    var userTrustScore by remember { mutableStateOf(98) } // Solution 18
    var wakeUpCount by remember { mutableStateOf(184) } // Solution 17
    var hasConsumerVerifiedLight by remember { mutableStateOf(false) } // Solution 23
    var selectedDialect by remember { mutableStateOf("Pidgin") } // Solution 20

    val filteredSolutions = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            PowerSector30Registry.ALL_30_SOLUTIONS
        } else {
            PowerSector30Registry.ALL_30_SOLUTIONS.filter { it.category == selectedCategoryFilter }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hub Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "30 POWER SECTOR SOLUTIONS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Automated consumer software fixing Nigeria's grid failures",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = GoldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
                ) {
                    Text(
                        text = "30/30 READY",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All 30 (${PowerSector30Registry.ALL_30_SOLUTIONS.size})", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_all_solutions")
                    )
                }
                items(SolutionCategory.entries) { category ->
                    val isSelected = selectedCategoryFilter == category
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategoryFilter = if (isSelected) null else category
                        },
                        label = {
                            Text(
                                text = "${category.id}. ${category.title.take(22)}...",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = GoldPrimary
                        )
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Solutions List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredSolutions, key = { it.problemNumber }) { item ->
                SolutionCardItem(
                    item = item,
                    isBatSignalMode = isBatSignalMode,
                    onToggleBatSignal = onToggleBatSignalMode,
                    onLaunchInteractiveAction = {
                        activeModalSolutionNumber = item.problemNumber
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // INTERACTIVE SOLUTION INSPECTION MODAL
    activeModalSolutionNumber?.let { solNum ->
        val solution = PowerSector30Registry.ALL_30_SOLUTIONS.firstOrNull { it.problemNumber == solNum }
        if (solution != null) {
            Dialog(onDismissRequest = { activeModalSolutionNumber = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(GoldPrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${solution.problemNumber}",
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = solution.solutionTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { activeModalSolutionNumber = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        // Problem Statement
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "GRID PROBLEM:",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = solution.problemStatement,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // App Solution
                        Surface(
                            color = EmeraldAccent.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "BRIGHT SOFTWARE SOLUTION:",
                                    color = EmeraldAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = solution.solutionDetail,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Solution-Specific Interactive Widget
                        when (solution.problemNumber) {
                            1 -> { // Tiered Urgency
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Test Urgency Tier Dispatching:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { selectedUrgencyTier = 1 },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (selectedUrgencyTier == 1) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (selectedUrgencyTier == 1) Color.Black else MaterialTheme.colorScheme.onSurface
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Tier 1 (House)", fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = { selectedUrgencyTier = 2 },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (selectedUrgencyTier == 2) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (selectedUrgencyTier == 2) Color.Black else MaterialTheme.colorScheme.onSurface
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Tier 2 (Street)", fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = { selectedUrgencyTier = 3 },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (selectedUrgencyTier == 3) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (selectedUrgencyTier == 3) Color.Black else MaterialTheme.colorScheme.onSurface
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Tier 3 (Substation)", fontSize = 10.sp)
                                        }
                                    }
                                    Text(
                                        text = when (selectedUrgencyTier) {
                                            1 -> "Tier 1: Single meter trip routed to local field technician (4hr SLA)."
                                            2 -> "Tier 2: Street transformer fuse/jumper fault. Clustered with 184 neighbors (2hr SLA)."
                                            else -> "Tier 3: 33kV Injection Feeder collapse. Critical escalation to Area Operations (1hr SLA)."
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            4 -> { // Red Button
                                Button(
                                    onClick = {
                                        activeModalSolutionNumber = null
                                        onOpenRedDangerSOS()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Emergency, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Critical Danger Red Button", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            5 -> { // Diagnostic Status
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Current Feeder Diagnostic Engine:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Surface(
                                        color = if (simulatedDiagnosticMode == "LOAD_SHEDDING") Color(0xFF8B5CF6).copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (simulatedDiagnosticMode == "LOAD_SHEDDING") Color(0xFF8B5CF6) else MaterialTheme.colorScheme.error)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (simulatedDiagnosticMode == "LOAD_SHEDDING") Icons.Default.Timeline else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (simulatedDiagnosticMode == "LOAD_SHEDDING") Color(0xFF8B5CF6) else MaterialTheme.colorScheme.error
                                            )
                                            Column {
                                                Text(
                                                    text = if (simulatedDiagnosticMode == "LOAD_SHEDDING") "TCN Grid Load-Shedding Active" else "Unplanned Distribution Fault",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = if (simulatedDiagnosticMode == "LOAD_SHEDDING") "Power cut intentionally per national quota. Expected return: 4:00 PM." else "Fuse blown on Feeder 4. Technician en route (ETA 45 mins).",
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            simulatedDiagnosticMode = if (simulatedDiagnosticMode == "LOAD_SHEDDING") "FAULT" else "LOAD_SHEDDING"
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Toggle Diagnostic State Simulation")
                                    }
                                }
                            }
                            6 -> { // USSD Offline
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Offline USSD / SMS Command Code:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        text = "*384*55*${userProfile.meterNumber}*1#",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = GoldPrimary
                                    )
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:*384*55*${userProfile.meterNumber}*1%23"))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Launch Offline USSD Code in Dialer")
                                    }
                                }
                            }
                            8 -> { // Bat-Signal Mode
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Bat-Signal Low Power Mode", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Monochrome, pure text OLED saver (<1% battery)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = isBatSignalMode,
                                        onCheckedChange = onToggleBatSignalMode,
                                        colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary)
                                    )
                                }
                            }
                            13 -> { // Estimated Bill Calculator
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Community Consumption Calculator:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    OutlinedTextField(
                                        value = estimatedBillInput,
                                        onValueChange = { estimatedBillInput = it },
                                        label = { Text("Your DisCo Estimated Bill (₦)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    val unmetered = estimatedBillInput.toDoubleOrNull() ?: 45000.0
                                    val avgNeighbors = 18200.0 // verified average from metered neighbors on same street
                                    val excess = (unmetered - avgNeighbors).coerceAtLeast(0.0)

                                    Surface(
                                        color = EmeraldAccent.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Verified Street Average (12 Neighbors): ₦18,200", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text("Calculated Arbitrary Overbilling: ₦${String.format("%,.2f", excess)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "NERC Capped Overbilling Dispute Document Generated!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                                    ) {
                                        Text("Generate NERC Capped Dispute Letter")
                                    }
                                }
                            }
                            17 -> { // Wake Up Street
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Broadcast to ${userProfile.transformerId}:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Invites all $wakeUpCount co-connected households on this transformer to open BRIGHT and co-sign the report in 1 tap.", fontSize = 11.sp)
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Broadcast sent! 42 neighbors opened BRIGHT.", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                                    ) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Send 'Wake Up the Street' Broadcast")
                                    }
                                }
                            }
                            19 -> { // Forum
                                Button(
                                    onClick = {
                                        activeModalSolutionNumber = null
                                        onOpenForum()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Forum, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Transformer ${userProfile.transformerId} Forum")
                                }
                            }
                            20 -> { // Voice Dialects
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Select Dialect for Speech-to-Ticket AI:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("Pidgin", "Hausa", "Yoruba", "Igbo").forEach { dialect ->
                                            FilterChip(
                                                selected = selectedDialect == dialect,
                                                onClick = { selectedDialect = dialect },
                                                label = { Text(dialect, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                    Text(
                                        text = when (selectedDialect) {
                                            "Pidgin" -> "Sample: \"Light don quench for our street since 2pm, transformer don spark!\""
                                            "Hausa" -> "Misali: \"Wutar lantarki ta dauke a unguwarmu, ga hayaki a transformer!\""
                                            "Yoruba" -> "Apeere: \"Iná ti kú láti ọ̀sán, transformer tún ń kọ iná!\""
                                            else -> "Ọmụma: \"Ọkụ agwala n'ogbe anyị kemgbe ehihie, transformer na-agba ọkụ!\""
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Voice parsed and mapped to SCADA ticket!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Simulate AI Voice Recording ($selectedDialect)")
                                    }
                                }
                            }
                            23 -> { // Consumer Closure
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Consumer Ticket Closure Gatekeeper:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Technicians cannot close a ticket alone. You verify whether power has truly returned.", fontSize = 11.sp)
                                    Button(
                                        onClick = {
                                            hasConsumerVerifiedLight = true
                                            Toast.makeText(context, "Verification confirmed! Ticket officially closed.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (hasConsumerVerifiedLight) EmeraldAccent else GoldPrimary,
                                            contentColor = Color.Black
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (hasConsumerVerifiedLight) "Light Verified (Ticket Closed)" else "Yes, My Light is Back!")
                                    }
                                }
                            }
                            27 -> { // Grid is Back Siren
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("'Grid is Back' Audio Siren:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Plays loud alert tone instantly when power returns to notify you to turn off your generator.", fontSize = 11.sp)
                                    Button(
                                        onClick = onPlaySirenAlarm,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Test Generator Shut-Off Siren")
                                    }
                                }
                            }
                            else -> {
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Executing ${solution.solutionTitle}...", Toast.LENGTH_SHORT).show()
                                        activeModalSolutionNumber = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                                ) {
                                    Text("Execute Automated Protocol", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SolutionCardItem(
    item: PowerProblemSolution,
    isBatSignalMode: Boolean,
    onToggleBatSignal: (Boolean) -> Unit,
    onLaunchInteractiveAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onLaunchInteractiveAction() }
            .testTag("solution_card_${item.problemNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Number & Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(GoldPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${item.problemNumber}",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = item.solutionTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item.regulatoryBadge?.let { badge ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Problem statement in subtle red tint
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = item.problemStatement,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Solution Detail
            Text(
                text = item.solutionDetail,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )

            // Card Action Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.category.title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                OutlinedButton(
                    onClick = onLaunchInteractiveAction,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Test Solution #${item.problemNumber}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
