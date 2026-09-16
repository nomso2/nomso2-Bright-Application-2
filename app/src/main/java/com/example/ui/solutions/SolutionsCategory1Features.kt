package com.example.ui.solutions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
// SOLUTION 1: TIERED URGENCY CATEGORISER
// ==========================================
@Composable
fun TieredUrgencyCategoriserFeature(
    userProfile: UserProfile,
    onDispatchTicket: ((String, String, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTier by remember { mutableStateOf(2) }
    var affectedHouseholds by remember { mutableStateOf(85f) }
    var hasHospitalOrClinic by remember { mutableStateOf(false) }
    var hasWaterBoard by remember { mutableStateOf(false) }
    var ticketDispatched by remember { mutableStateOf(false) }
    var generatedTicketId by remember { mutableStateOf("") }

    val priorityScore = remember(selectedTier, affectedHouseholds, hasHospitalOrClinic, hasWaterBoard) {
        val base = when (selectedTier) {
            1 -> 35
            2 -> 65
            else -> 85
        }
        val houseBonus = (affectedHouseholds / 500f * 20f).toInt()
        val criticalBonus = (if (hasHospitalOrClinic) 15 else 0) + (if (hasWaterBoard) 10 else 0)
        (base + houseBonus + criticalBonus).coerceAtMost(100)
    }

    val slaHours = when (selectedTier) {
        1 -> 4
        2 -> 2
        else -> 1
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "1. Tiered Urgency Dispatch Evaluator",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Select the electrical failure boundary to enforce statutory NERC response SLAs and auto-route field crew teams.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Tier Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple(1, "Tier 1\nHouse", "Single user / Meter trip"),
                Triple(2, "Tier 2\nStreet", "Transformer / Jumper"),
                Triple(3, "Tier 3\nDistrict", "33kV Feeder / Substation")
            ).forEach { (tier, title, desc) ->
                val isSelected = selectedTier == tier
                Button(
                    onClick = { selectedTier = tier },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(desc, fontSize = 8.sp, maxLines = 1)
                    }
                }
            }
        }

        // Affected Scope
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estimated Affected Households:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text("${affectedHouseholds.toInt()} Homes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
            }
            Slider(
                value = affectedHouseholds,
                onValueChange = { affectedHouseholds = it },
                valueRange = 1f..500f,
                colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary)
            )
        }

        // Critical Facilities
        Text("Critical Public Infrastructure Nearby:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = hasHospitalOrClinic,
                onCheckedChange = { hasHospitalOrClinic = it },
                colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
            )
            Text("Hospital / Primary Health Clinic (+15 Priority)", fontSize = 11.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = hasWaterBoard,
                onCheckedChange = { hasWaterBoard = it },
                colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
            )
            Text("State Water Board / Public Pumping Station (+10 Priority)", fontSize = 11.sp)
        }

        // Results Card
        Surface(
            color = if (priorityScore > 75) MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else GoldPrimary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (priorityScore > 75) MaterialTheme.colorScheme.error else GoldPrimary
            )
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NERC CALCULATED PRIORITY: $priorityScore / 100",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = if (priorityScore > 75) MaterialTheme.colorScheme.error else GoldPrimary
                    )
                    Text(
                        text = "Max SLA: $slaHours Hours",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                LinearProgressIndicator(
                    progress = { priorityScore / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = if (priorityScore > 75) MaterialTheme.colorScheme.error else GoldPrimary
                )
                Text(
                    text = when (selectedTier) {
                        1 -> "Individual prepaid meter trip. Routed to District Service Center with 4hr SLA window."
                        2 -> "Street distribution fault affecting ${affectedHouseholds.toInt()} homes on transformer ${userProfile.transformerId}. Dispatches Line Crew with 2hr SLA."
                        else -> "Critical 33kV bulk feeder tripping. Mandatory escalation to DisCo Head of Operations and NERC Grid Monitoring Desk (1hr SLA)."
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (ticketDispatched) {
            Surface(
                color = EmeraldAccent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent)
                    Column {
                        Text("Categorized Dispatch Active: $generatedTicketId", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldAccent)
                        Text("Dispatched to ${userProfile.discoCode} Dispatch NOC with Tier $selectedTier priority.", fontSize = 11.sp)
                    }
                }
            }
        } else {
            Button(
                onClick = {
                    val id = "DISP-${(1000..9999).random()}"
                    generatedTicketId = id
                    ticketDispatched = true
                    onDispatchTicket?.invoke(id, "Tier $selectedTier Outage", priorityScore)
                    Toast.makeText(context, "Dispatched $id to ${userProfile.discoCode} Control Room!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dispatch Priority Ticket to ${userProfile.discoCode}", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// SOLUTION 2: GPS GEOFENCING FOR FAULTS
// ==========================================
@Composable
fun GpsFaultGeofencingFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var geofenceRadiusMeters by remember { mutableStateOf(250f) }
    var isBroadcasting by remember { mutableStateOf(false) }

    val simulatedUserLat = 6.5244
    val simulatedUserLng = 3.3792
    val clusterCount = (geofenceRadiusMeters / 30).toInt().coerceAtLeast(3)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "2. GPS Geofencing & Fault Clustering Radar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Aggregates localized mobile reports around transformer coordinates to calculate the exact epicenter of blown fuses or fallen conductors.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Radar Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Text("Substation Epicenter:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text("${userProfile.transformerId} (Zone 4)", fontSize = 11.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Coordinates: ${String.format("%.4f", simulatedUserLat)}° N, ${String.format("%.4f", simulatedUserLng)}° E",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Simulated Geofence Graphic
                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size((geofenceRadiusMeters / 1000f * 80f + 25f).dp)
                                .border(1.5.dp, GoldPrimary, CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.15f), CircleShape)
                        )
                        Box(
                            modifier = Modifier.size(10.dp).background(Color.Red, CircleShape)
                        )
                        Text(
                            text = "$clusterCount Reports Clustered in ${geofenceRadiusMeters.toInt()}m Radius",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Geofence Radius:", fontSize = 11.sp)
                    Text("${geofenceRadiusMeters.toInt()} Meters", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                }
                Slider(
                    value = geofenceRadiusMeters,
                    onValueChange = { geofenceRadiusMeters = it },
                    valueRange = 50f..1000f,
                    colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    isBroadcasting = true
                    Toast.makeText(context, "Transmitted geofence centroid to DisCo Field Unit!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isBroadcasting) "Broadcasted ✓" else "Send GPS to Crew", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = {
                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$simulatedUserLat,$simulatedUserLng?q=$simulatedUserLat,$simulatedUserLng(Transformer+${userProfile.transformerId})"))
                    context.startActivity(mapIntent)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Open in Maps", fontSize = 11.sp)
            }
        }
    }
}

// ==========================================
// SOLUTION 3: AUTOMATED DISPATCH ROUTER ("UBER FOR ELECTRICIANS")
// ==========================================
@Composable
fun AutomatedDispatchRouterFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var etaMinutes by remember { mutableStateOf(24) }
    var crewStatus by remember { mutableStateOf("En Route to Site") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "3. Automated Dispatch Router (Uber for Line Crews)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Direct telemetry dispatch matching local feeder technicians with live vehicle registration, real-time ETA, and direct communication link.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Crew Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Engr. Babatunde Alabi", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("Senior Linesman • ID: ${userProfile.discoCode}-8821", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        color = EmeraldAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("DISPATCHED", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Service Vehicle: White Toyota Hilux (LAG-441-XY)", fontSize = 11.sp)
                    Text("Rating: 4.8 ★", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                }

                Surface(
                    color = GoldPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Assignment Status:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(crewStatus, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Live ETA:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$etaMinutes Mins", fontWeight = FontWeight.Black, fontSize = 14.sp, color = GoldPrimary)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:08030004921"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Crew", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            etaMinutes = (etaMinutes - 5).coerceAtLeast(3)
                            crewStatus = if (etaMinutes <= 5) "Arrived at Transformer ${userProfile.transformerId}" else "Navigating via Ikorodu Road"
                            Toast.makeText(context, "Updated ETA: $etaMinutes mins ($crewStatus)", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Refresh ETA", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// SOLUTION 4: CRITICAL DANGER RED BUTTON
// ==========================================
@Composable
fun CriticalDangerRedButtonFeature(
    onTriggerEmergency: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedHazard by remember { mutableStateOf("Fallen 33kV High-Tension Wire") }
    var emergencyTriggered by remember { mutableStateOf(false) }

    val hazards = listOf(
        "Fallen 33kV High-Tension Wire",
        "Transformer Oil Fire / Explosion",
        "Submerged Electric Pole in Floodwater",
        "Vandalized Live Jumper Cable Touching Roof"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "4. Critical Danger Red Button (Zero Harm Protocol)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = "Bypasses all queues. Triggers immediate automated line tripping protocol at DisCo Substation to prevent electrocution.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        hazards.forEach { hazard ->
            val isSelected = selectedHazard == hazard
            Surface(
                color = if (isSelected) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.error else Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { selectedHazard = hazard },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(hazard, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Surface(
            color = Color.Red.copy(alpha = 0.1f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                Text("MANDATORY SAFETY: Maintain 10-meter perimeter. Do NOT touch wet ground near conductors.", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = {
                emergencyTriggered = true
                onTriggerEmergency()
                Toast.makeText(context, "EMERGENCY: SCADA Feeder Trip Request sent for $selectedHazard!", Toast.LENGTH_LONG).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Emergency, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (emergencyTriggered) "EMERGENCY TRIP SIGNAL SENT" else "TRIGGER SCADA LINE TRIP SOS", fontWeight = FontWeight.Black)
        }
    }
}

// ==========================================
// SOLUTION 5: DIAGNOSTIC STATUS TRACKER
// ==========================================
@Composable
fun DiagnosticStatusTrackerFeature(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var diagnosticState by remember { mutableStateOf("LOAD_SHEDDING") }
    var allocatedMw by remember { mutableStateOf(14.2) }
    var peakDemandMw by remember { mutableStateOf(28.0) }
    var restoralTimeText by remember { mutableStateOf("4:30 PM (Scheduled)") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "5. Diagnostic Status Tracker (Load Shed vs Unplanned Fault)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Solves the mystery of whether power was cut intentionally due to national TCN load-shedding quota or an unplanned physical fault on your street.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { diagnosticState = "LOAD_SHEDDING" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (diagnosticState == "LOAD_SHEDDING") Color(0xFF8B5CF6) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (diagnosticState == "LOAD_SHEDDING") Color.White else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("TCN Load-Shedding", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { diagnosticState = "FAULT" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (diagnosticState == "FAULT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (diagnosticState == "FAULT") Color.White else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Unplanned Fault", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (diagnosticState == "LOAD_SHEDDING") {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = Color(0xFF8B5CF6))
                        Text("TCN Grid Allocation Quota Deficit", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF8B5CF6))
                    }
                    Text("Your injection feeder is shedding load to prevent national grid frequency collapse.", fontSize = 11.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Feeder Demand: ${peakDemandMw}MW", fontSize = 11.sp)
                        Text("Allocated: ${allocatedMw}MW (50.7%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { (allocatedMw / peakDemandMw).toFloat() },
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.fillMaxWidth().height(6.dp)
                    )
                    Text("Expected Restoration: $restoralTimeText", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldPrimary)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text("Physical Distribution Fault Detected", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                    }
                    Text("Protective relay trip code: OCR-51 (Phase B Overcurrent). DisCo field team notified.", fontSize = 11.sp)
                    Text("Transformer: ${userProfile.transformerId} • Estimated Repair: 1hr 15m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Subscribed to live SCADA Feeder restoration alert!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Subscribe to Real-Time Restoral Ping")
                }
            }
        }
    }
}
