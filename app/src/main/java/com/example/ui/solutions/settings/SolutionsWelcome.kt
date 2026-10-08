package com.example.ui.solutions.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.solutions.SolutionsPrefs
import com.example.model.UserProfile
import com.example.ui.solutions.common.BrightPermissions
import com.example.ui.solutions.common.SolutionsAutomation

/**
 * Runs once the user is signed in. Keeps background tools in step with Settings, and on the very
 * first launch shows ONE calm welcome screen, then asks only for notifications and location, one
 * after the other. Microphone and camera are asked only when the user first uses them.
 */
@Composable
fun SolutionsFirstRun(userProfile: UserProfile) {
    val context = LocalContext.current
    val prefs = remember { SolutionsPrefs(context) }
    var showWelcome by remember { mutableStateOf(!prefs.firstRunPermissionsAsked) }
    var showTour by remember { mutableStateOf(!prefs.tourShown) }

    LaunchedEffect(userProfile.feederBand) {
        val band = com.example.model.FeederBand.entries.firstOrNull { it.code == prefs.refundBand } ?: userProfile.feederBand
        prefs.promisedHours = band.minimumHours
        SolutionsAutomation.sync(context)
    }

    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        prefs.firstRunPermissionsAsked = true
        showWelcome = false
        SolutionsAutomation.sync(context)
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        locationLauncher.launch(BrightPermissions.LOCATION.toTypedArray())
    }

    if (!showWelcome) {
        // Right after the welcome and permissions: the ~30-second tour, shown once.
        if (showTour) {
            WelcomeTour(onDone = {
                prefs.tourShown = true
                showTour = false
            })
        }
        return
    }

    Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
        Box(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
                Text("Welcome to Bright", fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Bright will help you report faults and track your light.\n\nTap Allow on the next screens.",
                    fontSize = 20.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = {
                        val notif = BrightPermissions.NOTIFICATIONS
                        if (notif.isNotEmpty() && !BrightPermissions.hasAny(context, notif)) {
                            notificationLauncher.launch(notif.toTypedArray())
                        } else if (!BrightPermissions.hasAny(context, BrightPermissions.LOCATION)) {
                            locationLauncher.launch(BrightPermissions.LOCATION.toTypedArray())
                        } else {
                            prefs.firstRunPermissionsAsked = true
                            showWelcome = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) { Text("Continue", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
