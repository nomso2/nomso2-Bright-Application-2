package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StreetHazardPin
import com.example.model.UserProfile
import com.example.ui.theme.ElegantDarkBar
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Feature 10: Hanging High-Tension Wire & Street Hazard Pinning
 */
@Composable
fun PinStreetHazardDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onPinHazard: (
        title: String,
        hazardType: String,
        urgency: String,
        location: String,
        landmark: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val hazardTypes = listOf(
        "Dangling High-Tension Conductor",
        "Snapped Leaning Concrete Pole",
        "Submerged Flooded Feeder Pillar",
        "Sparking Low Transformer Drop Cable"
    )

    val urgencyLevels = listOf(
        "CRITICAL ELECTROCUTION RISK",
        "HIGH DANGER",
        "STRUCTURAL RISK"
    )

    var selectedType by remember { mutableStateOf(hazardTypes[0]) }
    var selectedUrgency by remember { mutableStateOf(urgencyLevels[0]) }
    var title by remember { mutableStateOf("Snapped 33kV Live Cable Across Road") }
    var location by remember { mutableStateOf(userProfile.streetAddress) }
    var landmark by remember { mutableStateOf("Near Central Motor Park & Primary School") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("pin_street_hazard_dialog"),
        containerColor = ElegantDarkBar,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Pin Street Electrical Hazard",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Broadcast Danger to Community & DisCo",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Warn neighbors and force rapid DisCo emergency response before electrocution or fire occurs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400Text
                )

                Text(
                    text = "HAZARD TYPE:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElegantGoldPrimary
                )

                hazardTypes.forEach { type ->
                    val isSelected = selectedType == type
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0x33EF4444) else Color(0xFF1E2430))
                            .border(1.dp, if (isSelected) Color(0xFFEF4444) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                selectedType = type
                                if (title.isEmpty() || hazardTypes.any { title.startsWith(it.take(15)) }) {
                                    title = type
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) Color.White else Slate400Text
                        )
                    }
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Hazard Summary") },
                    modifier = Modifier.fillMaxWidth().testTag("hazard_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = Slate100Text,
                        unfocusedTextColor = Slate100Text
                    )
                )

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Exact Street / Junction") },
                    modifier = Modifier.fillMaxWidth().testTag("hazard_location_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = Slate100Text,
                        unfocusedTextColor = Slate100Text
                    )
                )

                // Landmark
                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Nearby Landmark (e.g. Opposite Access Bank)") },
                    modifier = Modifier.fillMaxWidth().testTag("hazard_landmark_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = Slate100Text,
                        unfocusedTextColor = Slate100Text
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPinHazard(title, selectedType, selectedUrgency, location, landmark)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_pin_hazard_btn")
            ) {
                Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("BROADCAST HAZARD PIN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel", color = Slate400Text)
            }
        }
    )
}

/**
 * Community Street Hazards List Sheet / Dialog
 */
@Composable
fun StreetHazardsListDialog(
    hazards: List<StreetHazardPin>,
    onDismiss: () -> Unit,
    onOpenPinDialog: () -> Unit,
    onUpvoteHazard: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("street_hazards_list_dialog"),
        containerColor = ElegantDarkBar,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Pinned Street Hazards",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "${hazards.size} Live Community Danger Pins",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantGoldPrimary
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button to Pin New Hazard
                Button(
                    onClick = onOpenPinDialog,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("open_pin_hazard_from_list_btn")
                ) {
                    Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PIN NEW ELECTRICAL HAZARD", fontWeight = FontWeight.Bold)
                }

                if (hazards.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active electrical street hazards reported in your feeder zone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400Text
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(hazards, key = { it.id }) { hazard ->
                            HazardCardItem(
                                hazard = hazard,
                                onUpvote = { onUpvoteHazard(hazard.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2430), contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
fun HazardCardItem(
    hazard: StreetHazardPin,
    onUpvote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hazard_card_${hazard.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141820)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = hazard.urgency,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }

                if (hazard.isDispatched) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CREW DISPATCHED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            Text(
                text = hazard.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate100Text
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = ElegantGoldPrimary, modifier = Modifier.size(14.dp))
                Text(
                    text = "${hazard.location} (${hazard.landmark})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400Text
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reported by ${hazard.reportedBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500Text
                )

                OutlinedButton(
                    onClick = onUpvote,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantGoldPrimary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("upvote_hazard_${hazard.id}")
                ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verify Danger (${hazard.verifiedCount})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
