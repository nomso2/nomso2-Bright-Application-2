package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.UserProfile
import com.example.ui.solutions.settings.SolutionFeatureContent

/** One choice in the "Report a problem" sheet. [number] routes to the existing feature screen. */
data class ReportChoice(
    val number: Int,
    val title: String,
    val hint: String,
    val icon: ImageVector,
    val danger: Boolean = false
)

/** The five ways to report, in plain words. Same order as the welcome tour. */
val ReportChoices: List<ReportChoice> = listOf(
    ReportChoice(4, "Danger (SOS)", "Fallen or sparking wires", Icons.Filled.Warning, danger = true),
    ReportChoice(20, "Speak your report", "Say what happened", Icons.Filled.Mic),
    ReportChoice(16, "Send a photo", "Show the problem", Icons.Filled.CameraAlt),
    ReportChoice(2, "Pin where the fault is", "So the repair team finds it", Icons.Filled.MyLocation),
    ReportChoice(6, "Report by text message", "Works with no internet", Icons.Filled.Sms)
)

fun reportChoice(number: Int): ReportChoice? = ReportChoices.firstOrNull { it.number == number }

/** The one big button on Home. */
@Composable
fun ReportProblemButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .testTag("report_problem_button"),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(14.dp))
        Text("Report a problem", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

/** Bottom sheet with the five big choices. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportProblemSheet(onDismiss: () -> Unit, onChoose: (Int) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .testTag("report_problem_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "What do you want to do?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            ReportChoices.forEach { choice ->
                ReportChoiceRow(choice) { onChoose(choice.number) }
            }
        }
    }
}

@Composable
private fun ReportChoiceRow(choice: ReportChoice, onClick: () -> Unit) {
    val container = if (choice.danger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val content = if (choice.danger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    val badge = if (choice.danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val onBadge = if (choice.danger) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = container,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .semantics { contentDescription = "${choice.title}. ${choice.hint}" }
            .testTag("report_choice_${choice.number}")
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .background(badge, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (choice.danger) {
                    Text("SOS", color = onBadge, fontWeight = FontWeight.Black, fontSize = 17.sp)
                } else {
                    Icon(choice.icon, contentDescription = null, tint = onBadge, modifier = Modifier.size(30.dp))
                }
            }
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Text(choice.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = content)
                Text(choice.hint, fontSize = 16.sp, color = content)
            }
        }
    }
}

/**
 * Full-screen page for one report choice. It shows the existing feature screen for [number]
 * (the same code the tools use), so no reporting logic is duplicated here.
 */
@Composable
fun QuickActionScreen(
    number: Int,
    userProfile: UserProfile,
    isBatSignalMode: Boolean,
    onToggleBatSignalMode: (Boolean) -> Unit,
    onOpenHazardForm: () -> Unit,
    onOpenForum: () -> Unit,
    onClose: () -> Unit
) {
    val title = reportChoice(number)?.title
        ?: com.example.ui.solutions.settings.SolutionCatalog.info(number).title
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose, modifier = Modifier.size(56.dp).testTag("quick_action_close")) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(28.dp))
                    }
                    Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface)
                }
                HorizontalDivider()
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    SolutionFeatureContent(
                        number = number,
                        userProfile = userProfile,
                        isBatSignalMode = isBatSignalMode,
                        onToggleBatSignalMode = onToggleBatSignalMode,
                        onOpenHazardForm = { onClose(); onOpenHazardForm() },
                        onOpenForum = { onClose(); onOpenForum() }
                    )
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}
