package com.example.ui.solutions

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPrimary

// ==========================================
// SOLUTION 21: ANONYMOUS WHISTLEBLOWER
// ==========================================
@Composable
fun AnonymousWhistleblowerFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var staffNameOrVehicle by remember { mutableStateOf("Technician with Van EK-441") }
    var bribeAmountDemanded by remember { mutableStateOf("15000") }
    var bribeType by remember { mutableStateOf("Fuel for DisCo Service Truck") }
    var isSubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "21. Anonymous Whistleblower (Anti-Extortion Vault)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "NERC regulations forbid charging customers for repairs, fuses, or fuel. Report extortion anonymously directly to the NERC Enforcement Division.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = staffNameOrVehicle,
                    onValueChange = { staffNameOrVehicle = it },
                    label = { Text("DisCo Officer Name or Vehicle Plate", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )

                OutlinedTextField(
                    value = bribeAmountDemanded,
                    onValueChange = { bribeAmountDemanded = it },
                    label = { Text("Extortion Amount Demanded (₦)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                )

                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("LEGAL NOTICE (NERC Act 2023):", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                        Text("DisCos are funded through tariffs to provide all cables, transformers, and fuel. Demanding payment from consumers is a criminal offense.", fontSize = 10.sp)
                    }
                }

                Button(
                    onClick = {
                        isSubmitted = true
                        Toast.makeText(context, "Encrypted whistleblower dossier transmitted to NERC Enforcement!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSubmitted) "Dossier Transmitted Anonymously ✓" else "Transmit Encrypted Dossier to NERC", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 22: PIZZA-STYLE 5-STAGE TRACKER
// ==========================================
@Composable
fun PizzaStyleDeliveryTrackerFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stages = listOf(
        Pair("1. Outage Logged & Geotagged", "09:12 AM ✓"),
        Pair("2. DisCo Control Room Triaged", "09:28 AM ✓"),
        Pair("3. Crew Dispatched (Van LAG-441-XY)", "09:55 AM ✓"),
        Pair("4. Linesmen on Transformer Pole", "10:20 AM (In Progress) ⚙"),
        Pair("5. Feeder Energized & Customer Verified", "Pending ⏳")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "22. 'Pizza-Style' 5-Stage Restoration Tracker",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Live milestone progress tracking showing exact technician movements and repair stages instead of vague customer care promises.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stages.forEachIndexed { index, (stage, time) ->
                    val isDone = index < 3
                    val isActive = index == 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (isDone) EmeraldAccent else if (isActive) GoldPrimary else Color.Gray.copy(alpha = 0.3f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDone || isActive) Color.Black else Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stage, fontSize = 11.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, color = if (isActive) GoldPrimary else MaterialTheme.colorScheme.onSurface)
                            Text(time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Pinged DisCo Field Supervisor for progress update!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ping Field Supervisor for Live Radio Status", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 23: CONSUMER TICKET CLOSURE VERIFICATION
// ==========================================
@Composable
fun ConsumerClosureVerificationFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasVerifiedLight by remember { mutableStateOf(false) }
    var hasRejectedFakeClosure by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "23. Consumer Ticket Closure Verification Gatekeeper",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "DisCos can no longer mark tickets as 'Resolved' from their office. Tickets remain open until verified by real residents on the transformer.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = GoldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Ticket #TKT-8902 Pending Customer Confirmation", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GoldPrimary)
                            Text("DisCo claimed repair is complete. Please verify actual voltage.", fontSize = 10.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            hasVerifiedLight = true
                            hasRejectedFakeClosure = false
                            Toast.makeText(context, "Restoration verified! Ticket closed with customer approval.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (hasVerifiedLight) "Verified ✓" else "Yes, Light is Back!", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            hasRejectedFakeClosure = true
                            hasVerifiedLight = false
                            Toast.makeText(context, "Premature closure REJECTED! DisCo penalized for false resolution.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (hasRejectedFakeClosure) "Closure Blocked!" else "No, Still in Darkness", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 24: FAULT HISTORY LOG
// ==========================================
@Composable
fun FaultHistoryLogFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var petitionSent by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "24. Recurrent Infrastructure Failure Auditor",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Tracks breakdown frequency of transformer ${userProfile.transformerId}. When failures exceed 2 per month, automatically triggers statutory transformer replacement demand.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Recent Breakdowns Log (Past 30 Days):", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("• 3 Days Ago: Blown 400A HRC Drop-out fuse (14 hrs outage)", fontSize = 10.sp)
                    Text("• 11 Days Ago: Transformer oil insulation breakdown (42 hrs outage)", fontSize = 10.sp)
                    Text("• 22 Days Ago: 33kV jumper burn-off (18 hrs outage)", fontSize = 10.sp)
                }

                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Text("3 Failures in 30 Days: Exceeds NERC Reliability Benchmark. Classified as Obsolete Capital Asset.", fontSize = 10.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        petitionSent = true
                        Toast.makeText(context, "Statutory Demand for Full 500kVA Replacement dispatched to NERC!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (petitionSent) "Replacement Demand Filed ✓" else "File Demand for Complete Transformer Replacement", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 25: DISCO INVENTORY REQUEST & SPARE PARTS MONITOR
// ==========================================
@Composable
fun InventoryRequestMonitorFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPart by remember { mutableStateOf("500kVA Transformer Oil (200L)") }

    val stockInventory = mapOf(
        "500kVA Transformer Oil (200L)" to Pair("Ijora Central Warehouse", "42 Drums Available"),
        "400A High-Rupturing Capacity Fuse" to Pair("Oshodi Zonal Store", "118 Units Available"),
        "300mm² Aluminum Overhead Cable" to Pair("Ikeja Central Yard", "1,200 Meters Available")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "25. DisCo Spare Parts Stock Transparency Tracker",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "When technicians claim 'we have no materials in store' to demand bribes, check real-time warehouse inventory to expose artificial delays.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stockInventory.keys.forEach { part ->
                    val isSelected = selectedPart == part
                    Surface(
                        color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(part, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            Button(
                                onClick = { selectedPart = part },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Inspect", fontSize = 10.sp)
                            }
                        }
                    }
                }

                val currentStock = stockInventory[selectedPart]
                currentStock?.let { (warehouse, count) ->
                    Surface(
                        color = EmeraldAccent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("OFFICIAL DISCO WAREHOUSE AUDIT:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = EmeraldAccent)
                            Text("• Location: $warehouse", fontSize = 10.sp)
                            Text("• Stock Level: $count (IN STOCK)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                            Text("• Status: False claim by local crew. Report extortion.", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Inventory report shared to Estate Exco WhatsApp!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Expose Stock Availability to Estate Exco", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
