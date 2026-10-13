package com.example.ui.solutions.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.cloud.BrightFirebase
import com.example.data.cloud.CommunityFeed
import com.example.data.database.AppDatabase
import com.example.data.database.CommunityStatsEntity
import com.example.model.UserProfile

/**
 * Community averages from neighbours' anonymised reports (Firestore community_reports), for the
 * user's transformer, feeder and DisCo over the last 30 days. Cached on the phone for offline.
 * Shows "No neighbours have reported yet" when there is nothing real to show.
 */
@Composable
fun CommunityAveragesCard(userProfile: UserProfile) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).communityStatsDao() }
    val scopes = listOf(
        Triple("Your transformer", "transformerId", userProfile.transformerId),
        Triple("Your feeder", "feederName", userProfile.feederName),
        Triple("Your DisCo", "discoCode", userProfile.discoCode)
    ).filter { it.third.isNotBlank() }

    LaunchedEffect(userProfile.transformerId, userProfile.feederName, userProfile.discoCode) {
        if (BrightFirebase.isConfigured) {
            scopes.forEach { (_, field, value) -> CommunityFeed.refreshStats(context, field, value) }
        }
    }

    SectionCard {
        Text("Community averages (last 30 days)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (!BrightFirebase.isConfigured) {
            Text("Community figures need Bright's online service, which isn't switched on in this version yet. Your own log above is real.",
                fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        var anyData = false
        scopes.forEach { (label, field, value) ->
            val flow = remember(field, value) { dao.observe(CommunityFeed.scopeKey(field, value)) }
            val stats by flow.collectAsState(initial = null)
            val s: CommunityStatsEntity? = stats
            if (s != null && s.reportCount > 0) {
                anyData = true
                Text("$label · $value", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                StatRow("Reports", "${s.reportCount} from ${s.neighbourCount} resident${if (s.neighbourCount == 1) "" else "s"}")
                s.avgSupplyHours?.let { StatRow("Average supply", "${Fmt.hours(it)}/day (outage ${Fmt.hours((24 - it).coerceAtLeast(0.0))}/day)") }
                s.avgRestoreHours?.let { StatRow("Average time to restore", Fmt.hours(it)) }
                if (field != "transformerId") s.reportsPerTransformer?.let { StatRow("Reports per transformer", "%.1f".format(it)) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Updated ${Fmt.dateTime(s.fetchedAt)} · from Bright users' reports", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier)
                }
            }
        }
        if (!anyData) {
            Text("No neighbours have reported yet.", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
