package com.example.ui.solutions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.solutions.common.FeatureHeader
import com.example.ui.solutions.common.Fmt
import com.example.ui.solutions.common.NumberField
import com.example.ui.solutions.common.SectionCard
import com.example.ui.solutions.common.SolutionIntents
import com.example.ui.solutions.common.StatRow
import com.example.ui.solutions.common.rememberSolutionsPrefs

/** A regulatory change explained in plain words, with the tariff before/after when it's a price change. */
data class TariffNewsItem(
    val id: String,
    val date: String,
    val headline: String,
    val plain: String,
    val oldTariff: Double?,
    val newTariff: Double?,
    val sourceUrl: String
)

interface TariffNewsSource {
    fun items(): List<TariffNewsItem>
}

/** Curated from NERC orders; checked Oct 2026. Replace with a live feed when NERC/Bright publishes one. */
object CuratedTariffNews : TariffNewsSource {
    override fun items() = listOf(
        TariffNewsItem(
            "2024-07-banda", "1 Jul 2024", "Band A goes up to ₦209.50/kWh",
            "If you're on a Band A feeder (20+ hours a day), each unit now costs ₦209.50 before VAT. Bands B-E stay frozen at their December 2022 rates. Still the Band A rate in NERC's 2026 orders.",
            206.80, 209.50, "https://nerc.gov.ng/media/multi-year-tariff-order-myto-2024"
        ),
        TariffNewsItem(
            "2024-05-banda", "6 May 2024", "Band A cut to ₦206.80/kWh",
            "NERC reduced the Band A price because the naira strengthened in April. Prepaid users saw it on their next token.",
            225.00, 206.80, "https://nerc.gov.ng/media/multi-year-tariff-order-myto-2024"
        ),
        TariffNewsItem(
            "2024-04-banda", "3 Apr 2024", "Band A jumps to ₦225/kWh",
            "Band A customers went from roughly ₦66-68 to ₦225 per unit - more than 3x. In return Band A must get at least 20 hours of supply a day.",
            68.00, 225.00, "https://nerc.gov.ng/media/multi-year-tariff-order-myto-2024"
        ),
        TariffNewsItem(
            "band-a-compensation", "NERC Order on compensation", "Short supply on Band A? You're owed compensation",
            "A Band A feeder must average 20 hours a day. If yours gets less, you're owed the difference between what you paid and the tariff for the service you actually got, and a feeder below 20 hours for 7 days in a row should be downgraded. Track it in #12.",
            null, null, "https://nerc.gov.ng"
        ),
        TariffNewsItem(
            "electricity-act-2023", "June 2023", "States can now regulate their own electricity",
            "The Electricity Act 2023 lets each state set up its own regulator. Enugu was among the first: MainPower now serves Enugu State under the state regulator. Your tariff and complaints route may change if your state takes over.",
            null, null, "https://nerc.gov.ng"
        )
    )
}

// SOLUTION 30: TARIFF FLASH NEWS FEED
@Composable
fun TariffFlashNewsFeature() {
    val context = LocalContext.current
    val prefs = rememberSolutionsPrefs()
    var read by remember { mutableStateOf(prefs.readNewsIds) }
    var units by remember { mutableStateOf(prefs.unitsPerMonthKwh.toString()) }
    val items = remember { CuratedTariffNews.items() }
    val kwh = units.toDoubleOrNull() ?: 0.0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureHeader(30, "Tariff Flash News Feed", "Regulatory changes in plain words - and what each one costs you, based on the units you use.")
        NumberField("Units you use per month", units, { units = it }, suffix = "kWh")
        Text("${items.count { it.id !in read }} unread", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        items.forEach { item ->
            val unread = item.id !in read
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.date, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    if (unread) Text("NEW", fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
                Text(item.headline, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(item.plain, fontSize = 16.sp)
                if (item.oldTariff != null && item.newTariff != null && kwh > 0) {
                    val before = item.oldTariff * kwh
                    val after = item.newTariff * kwh
                    StatRow("Your month before", Fmt.naira(before))
                    StatRow("Your month after", Fmt.naira(after))
                    StatRow(if (after >= before) "Extra per month" else "Saving per month", Fmt.naira(kotlin.math.abs(after - before)),
                        if (after > before) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { SolutionIntents.openUrl(context, item.sourceUrl) }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null); Text(" Source")
                    }
                    if (unread) OutlinedButton(onClick = {
                        read = read + item.id
                        prefs.readNewsIds = read
                    }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Mark read") }
                }
            }
        }
        Text("Curated from NERC tariff orders, checked Oct 2026. Tap Source to read the order.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
