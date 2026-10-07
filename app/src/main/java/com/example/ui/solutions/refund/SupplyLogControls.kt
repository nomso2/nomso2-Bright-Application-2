package com.example.ui.solutions.refund

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.solutions.DayHours
import com.example.data.solutions.SolutionsPrefs
import com.example.data.solutions.SupplyEventEntity
import com.example.ui.solutions.common.Fmt
import com.example.ui.solutions.common.SirenPlayer
import com.example.ui.solutions.common.SupplyLogger
import com.example.ui.solutions.common.rememberSolutionsDao
import com.example.ui.theme.extendedColors
import kotlinx.coroutines.launch

/**
 * The two big "Light ON" / "Light OFF" buttons. Every supply-hours feature (refund tracker,
 * band auditor, siren, optimizer) reads the same log.
 */
@Composable
fun SupplyLogButtons(events: List<SupplyEventEntity>) {
    val context = LocalContext.current
    val dao = rememberSolutionsDao()
    val scope = rememberCoroutineScope()
    val last = events.lastOrNull()

    fun log(on: Boolean) {
        scope.launch {
            SupplyLogger.log(context, on, "APP")
            if (on && SolutionsPrefs(context).sirenArmed) SirenPlayer.play(context, 10)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            when {
                last == null -> "Nothing logged yet. Tap when your light comes on or goes off."
                last.isOn -> "Light ON since ${Fmt.dateTime(last.timestamp)}"
                else -> "Light OFF since ${Fmt.dateTime(last.timestamp)}"
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { log(true) },
                enabled = last?.isOn != true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.extendedColors.success,
                    contentColor = MaterialTheme.extendedColors.onSuccess
                ),
                modifier = Modifier.weight(1f).heightIn(min = 64.dp)
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null)
                Text("  Light ON", fontWeight = FontWeight.Black)
            }
            Button(
                onClick = { log(false) },
                enabled = last?.isOn != false,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                ),
                modifier = Modifier.weight(1f).heightIn(min = 64.dp)
            ) {
                Icon(Icons.Default.PowerOff, contentDescription = null)
                Text("  Light OFF", fontWeight = FontWeight.Black)
            }
        }
        if (last != null) {
            TextButton(onClick = { scope.launch { dao.deleteSupplyEvent(last) } }, modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Undo last entry (${Fmt.time(last.timestamp)})")
            }
        }
    }
}

/** Ring of today's supply hours out of 24, with a tick at the band's promised hours. */
@Composable
fun HoursRing(hoursToday: Double, promisedHours: Int) {
    val good = MaterialTheme.extendedColors.success
    val bad = MaterialTheme.colorScheme.error
    val track = MaterialTheme.colorScheme.outlineVariant
    val tick = MaterialTheme.colorScheme.onSurface
    val color = if (hoursToday >= promisedHours) good else bad
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp).semantics {
        contentDescription = "Today: ${Fmt.hours(hoursToday)} of supply. Promise is $promisedHours hours."
    }) {
        Canvas(Modifier.size(140.dp)) {
            val stroke = 14.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(track, -90f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(color, -90f, (hoursToday / 24.0 * 360).toFloat().coerceAtMost(360f), false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            val angle = Math.toRadians(-90.0 + promisedHours / 24.0 * 360)
            val r = size.width / 2
            val cx = size.width / 2
            val cy = size.height / 2
            drawLine(
                tick,
                Offset(cx + ((r - stroke * 1.4f) * Math.cos(angle)).toFloat(), cy + ((r - stroke * 1.4f) * Math.sin(angle)).toFloat()),
                Offset(cx + (r * Math.cos(angle)).toFloat(), cy + (r * Math.sin(angle)).toFloat()),
                strokeWidth = 4f
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(Fmt.hours(hoursToday), fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text("today / ${promisedHours}h", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Bars of daily hours with a line at the promised hours. */
@Composable
fun DailyHoursChart(days: List<DayHours>, promisedHours: Int) {
    val good = MaterialTheme.extendedColors.success
    val bad = MaterialTheme.colorScheme.error
    val line = MaterialTheme.colorScheme.onSurface
    val below = days.dropLast(1).count { it.hours < promisedHours }
    Canvas(Modifier.fillMaxWidth().height(110.dp).semantics {
        contentDescription = "Daily supply chart: ${days.size} days, $below complete days below $promisedHours hours"
    }) {
        if (days.isEmpty()) return@Canvas
        val slot = size.width / days.size
        val barW = slot * 0.7f
        days.forEachIndexed { i, d ->
            val h = (d.hours / 24.0 * size.height).toFloat()
            drawRect(
                if (d.hours >= promisedHours) good else bad,
                topLeft = Offset(i * slot + (slot - barW) / 2, size.height - h),
                size = Size(barW, h)
            )
        }
        val y = size.height - promisedHours / 24f * size.height
        drawLine(line, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
    }
}
