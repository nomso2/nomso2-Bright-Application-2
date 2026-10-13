package com.example.ui.meter

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Live camera scan of the meter nameplate barcode / QR (CameraX + ML Kit). Calls [onResult]
 * with the raw payload of the first code found. Needs the CAMERA permission already granted.
 */
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun NameplateScannerDialog(onResult: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val done = remember { AtomicBoolean(false) }
    val scanner = remember { BarcodeScanning.getClient() }
    val previewView = remember { PreviewView(context) }

    DisposableEffect(Unit) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { proxy ->
                    val media = proxy.image
                    if (media == null || done.get()) {
                        proxy.close()
                        return@setAnalyzer
                    }
                    val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener { codes ->
                            val raw = codes.firstNotNullOfOrNull { it.rawValue?.takeIf { v -> v.isNotBlank() } }
                            if (raw != null && done.compareAndSet(false, true)) {
                                ContextCompat.getMainExecutor(context).execute { onResult(raw) }
                            }
                        }
                        .addOnCompleteListener { proxy.close() }
                }
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            } catch (e: Exception) {
                // Camera unavailable: the user can still type the number.
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            try { providerFuture.get().unbindAll() } catch (_: Exception) { }
            executor.shutdown()
            scanner.close()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Scan the meter nameplate") },
        text = {
            Column {
                Text("Point the camera at the barcode or QR code on the meter's label.", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(Modifier.fillMaxWidth().height(260.dp).padding(top = 8.dp)) {
                    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().height(260.dp))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** Pulls an 11-13 digit meter number out of a scanned payload, if there is one. */
fun meterNumberFromPayload(raw: String): String? =
    Regex("(?<!\\d)\\d{11,13}(?!\\d)").find(raw.replace(" ", "").replace("-", ""))?.value
