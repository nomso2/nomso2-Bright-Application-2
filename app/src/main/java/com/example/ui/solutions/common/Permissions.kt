package com.example.ui.solutions.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

object BrightPermissions {
    val LOCATION = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    val MICROPHONE = listOf(Manifest.permission.RECORD_AUDIO)
    val CAMERA = listOf(Manifest.permission.CAMERA)

    /** Notifications only need a runtime grant on Android 13+. */
    val NOTIFICATIONS: List<String> =
        if (Build.VERSION.SDK_INT >= 33) listOf(Manifest.permission.POST_NOTIFICATIONS) else emptyList()

    fun hasAny(context: Context, permissions: List<String>): Boolean =
        permissions.isEmpty() || permissions.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
}

/**
 * Returns an action that runs [onGranted] once any of [permissions] is granted. The first time
 * it explains why ([rationale]) before the system prompt; if the user says no it calls
 * [onDenied] (so the feature can fall back) and offers a way to Settings.
 */
@Composable
fun rememberPermissionAction(
    permissions: List<String>,
    title: String,
    rationale: String,
    onDenied: () -> Unit = {},
    onGranted: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val grantedCallback by rememberUpdatedState(onGranted)
    val deniedCallback by rememberUpdatedState(onDenied)
    var showRationale by remember { mutableStateOf(false) }
    var showDenied by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) {
            grantedCallback()
        } else {
            showDenied = true
            deniedCallback()
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(title, fontSize = 22.sp) },
            text = { Text(rationale + "\n\nTap Allow on the next screen.", fontSize = 16.sp) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    launcher.launch(permissions.toTypedArray())
                }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Continue", fontSize = 16.sp) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    deniedCallback()
                }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Not now", fontSize = 16.sp) }
            }
        )
    }

    if (showDenied) {
        AlertDialog(
            onDismissRequest = { showDenied = false },
            title = { Text("Not turned on yet", fontSize = 22.sp) },
            text = { Text("That's okay. You can turn it on in Settings whenever you like.", fontSize = 16.sp) },
            confirmButton = {
                TextButton(onClick = {
                    showDenied = false
                    SolutionIntents.openAppSettings(context)
                }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Turn on in Settings", fontSize = 16.sp) }
            },
            dismissButton = { TextButton(onClick = { showDenied = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Not now", fontSize = 16.sp) } }
        )
    }

    return {
        if (BrightPermissions.hasAny(context, permissions)) grantedCallback() else showRationale = true
    }
}
