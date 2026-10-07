package com.example.ui.solutions.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Header of a feature page. Always shows the plain-language name from SolutionCatalog. */
@Suppress("UNUSED_PARAMETER")
@Composable
fun FeatureHeader(number: Int, title: String, summary: String) {
    val info = com.example.ui.solutions.settings.SolutionCatalog.info(number)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = info.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(summary, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Marks anything not yet backed by a DisCo / TCN / NERC API. */
@Composable
fun DemoDataBadge(text: String = "Demo data") {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.semantics { contentDescription = "$text: not live yet" }
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}

@Composable
fun InfoNote(text: String, isWarning: Boolean = false) {
    Surface(
        color = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = if (isWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(10.dp)
        )
    }
}

/** Full-width button with a 48dp minimum height. */
@Composable
fun ActionButton(
    text: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    danger: Boolean = false,
    outlined: Boolean = false
) {
    val content: @Composable () -> Unit = {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Box(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) { content() }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
            colors = if (danger) ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) else ButtonDefaults.buttonColors()
        ) { content() }
    }
}

@Composable
fun NumberField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, suffix: String = "") {
    OutlinedTextField(
        value = value,
        onValueChange = { new -> onValueChange(new.filter { it.isDigit() || it == '.' }) },
        label = { Text(label) },
        suffix = { if (suffix.isNotEmpty()) Text(suffix) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun TextInput(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, singleLine: Boolean = true, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}

/** Vertical stepper for ticket / claim progress. */
@Composable
fun StatusStepper(steps: List<String>, current: Int, timestamps: Map<Int, String> = emptyMap()) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.semantics {
        contentDescription = "Progress: step ${current + 1} of ${steps.size}, ${steps.getOrNull(current) ?: ""}"
    }) {
        steps.forEachIndexed { i, label ->
            val done = i <= current
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.size(22.dp).background(
                        if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${i + 1}", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = if (done) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column {
                    Text(
                        label, fontSize = 16.sp,
                        fontWeight = if (i == current) FontWeight.Bold else FontWeight.Normal,
                        color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    timestamps[i]?.let { Text(it, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}

@Composable
fun PhotoThumb(path: String, description: String, modifier: Modifier = Modifier) {
    val bmp = remember(path) { EvidenceFiles.thumbnail(path) }
    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = description,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Text("Photo not found", fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun StatRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun ChoiceRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    // Wraps onto as many rows as needed; each chip is a 48dp touch target.
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { opt ->
                    val isSel = opt == selected
                    Surface(
                        onClick = { onSelect(opt) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).semantics {
                            contentDescription = if (isSel) "$opt, selected" else opt
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(6.dp)) {
                            Text(
                                opt, fontSize = 15.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                repeat(3 - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

object Fmt {
    private val naira = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }

    fun naira(v: Double): String = "₦" + naira.format(v)
    fun hours(v: Double): String = String.format(Locale.US, "%.1f h", v)
    fun dateTime(t: Long): String = SimpleDateFormat("d MMM, h:mm a", Locale.UK).format(Date(t))
    fun date(t: Long): String = SimpleDateFormat("d MMM yyyy", Locale.UK).format(Date(t))
    fun day(t: Long): String = SimpleDateFormat("EEE d", Locale.UK).format(Date(t))
    fun time(t: Long): String = SimpleDateFormat("h:mm a", Locale.UK).format(Date(t))
    fun duration(ms: Long): String {
        val s = ms / 1000
        return if (s >= 3600) "${s / 3600}h ${(s % 3600) / 60}m" else "${s / 60}:${String.format(Locale.US, "%02d", s % 60)}"
    }
}
