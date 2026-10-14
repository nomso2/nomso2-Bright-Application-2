package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DisCoContact
import com.example.model.DisCoContacts
import com.example.model.MaintenanceAlert
import com.example.model.PhoneNumbers
import com.example.model.StateDisCoMap
import com.example.model.UserProfile

/**
 * Planned maintenance notices.
 *
 * DisCos announce planned outages on their own websites and social pages; there is no verified
 * machine-readable feed, so Bright does not invent notices. [alerts] only ever holds notices from
 * a real source (none is wired up yet), so today this shows the empty state with the official
 * channels of the user's DisCo, taken from the app's verified DisCo contact data.
 */
@Composable
fun PlannedMaintenanceNoticesCard(
    alerts: List<MaintenanceAlert>,
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contacts: List<DisCoContact> = remember(userProfile.state, userProfile.discoCode) {
        val byState = StateDisCoMap.forState(userProfile.state)?.contacts.orEmpty()
        if (byState.isNotEmpty()) byState else listOfNotNull(DisCoContacts.forCode(userProfile.discoCode))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("planned_maintenance_notices_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "PLANNED MAINTENANCE NOTICES",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (alerts.isNotEmpty()) {
                alerts.forEach { a ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(a.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${a.discoCode} · ${a.startDate}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        if (a.affectedFeeders.isNotBlank()) {
                            Text(a.affectedFeeders, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (a.reason.isNotBlank()) {
                            Text(a.reason, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                Text(
                    text = "No planned maintenance notices yet.",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Check your DisCo's official page or social channels for planned outages in your area.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                contacts.forEach { c ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(c.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (c.website.isNotBlank()) {
                                Button(
                                    onClick = {
                                        try {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(c.website)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            )
                                        } catch (e: Exception) {
                                            Toast.makeText(context, c.website, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Official website", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (c.primaryPhone.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            context.startActivity(
                                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + PhoneNumbers.toDialable(c.primaryPhone)))
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            )
                                        } catch (e: Exception) {
                                            Toast.makeText(context, c.primaryPhone, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call ${c.primaryPhone}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                if (contacts.isNotEmpty()) {
                    Text(
                        text = DisCoContacts.SOURCE_FOOTER,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
