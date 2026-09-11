package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.ElegantBluePhase
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkCardEnd
import com.example.ui.theme.ElegantDarkCardStart
import com.example.ui.theme.ElegantGoldPrimary
import com.example.ui.theme.ElegantGreenLive
import com.example.ui.theme.Slate100Text
import com.example.ui.theme.Slate300Text
import com.example.ui.theme.Slate400Text
import com.example.ui.theme.Slate500Text

@Composable
fun MeterProfileHeader(
    profile: UserProfile,
    onEditProfileClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("meter_profile_header_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ElegantDarkCardStart, ElegantDarkCardEnd)
                    )
                )
                .border(
                    width = 1.dp,
                    color = ElegantDarkBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Meter ID + Verified Badge, with Live Feed Pill directly underneath
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (profile.isPrepaid) "METER ID • PREPAID RESIDENTIAL" else "METER ID • POSTPAID ACCOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = Slate400Text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatMeterNumber(profile.meterNumber),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 19.sp
                                ),
                                color = Slate100Text
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Meter in DisCo Database",
                                tint = ElegantGoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Live feed pill with emerald pulse, positioned directly under verified meter number
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0x1A22C55E))
                                .border(1.dp, Color(0x3322C55E), RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(ElegantGreenLive)
                                )
                                Text(
                                    text = "LIVE FEED • VERIFIED ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.6.sp
                                    ),
                                    color = Color(0xFF4ADE80),
                                    softWrap = false,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Edit button on the top right
                    IconButton(
                        onClick = onEditProfileClicked,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x14FFFFFF))
                            .border(1.dp, Color(0x22FFFFFF), CircleShape)
                            .testTag("edit_meter_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Meter or Address",
                            tint = Slate300Text,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phase Status row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color(0x1FFFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(Color(0x0DFFFFFF))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ElegantBluePhase)
                            )
                            Text(
                                text = "Phase Status: ",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Slate400Text,
                                softWrap = false,
                                maxLines = 1
                            )
                            Text(
                                text = "Stable (234V Balanced)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = Slate100Text,
                                softWrap = false,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = profile.feederBand.code,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = ElegantGoldPrimary,
                            softWrap = false,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Address & DisCo info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Service Address",
                        tint = Slate400Text,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${profile.streetAddress}, ${profile.lga}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Slate400Text,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Information Pills Row: Side by side (sideways), horizontally scrollable so Households and all text never wrap straight down
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoPill(
                        label = "DisCo",
                        value = profile.discoCode,
                        containerColor = Color(0x14FFFFFF),
                        contentColor = ElegantGoldPrimary
                    )

                    InfoPill(
                        label = "Transformer",
                        value = profile.transformerId,
                        containerColor = Color(0x14FFFFFF),
                        contentColor = Slate100Text
                    )

                    InfoPill(
                        label = "Households",
                        value = "${profile.connectedHouseholdsCount} Connected",
                        containerColor = Color(0x14FFFFFF),
                        contentColor = Color(0xFF60A5FA)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoPill(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Slate400Text,
                softWrap = false,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = contentColor,
                softWrap = false,
                maxLines = 1
            )
        }
    }
}

private fun formatMeterNumber(num: String): String {
    val clean = num.replace(" ", "")
    return when {
        clean.length >= 11 -> "${clean.substring(0, 4)} • ${clean.substring(4, 8)} • ${clean.substring(8)}"
        clean.length > 8 -> "${clean.substring(0, 4)} • ${clean.substring(4, 8)} • ${clean.substring(8)}"
        clean.length > 4 -> "${clean.substring(0, 4)} • ${clean.substring(4)}"
        else -> clean
    }
}
