package com.google.ar.core.examples.kotlin.ui.theme.screens


import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun TextRecognitionScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var recognizedText by remember { mutableStateOf("No text recognized yet!") }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                lifecycleOwner,
                cameraExecutor,
                onTextRecognized = { text ->
                    recognizedText = text
                }
            )
            Text(
                text = recognizedText,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    } else {
        // Request camera permission
        Text("Camera permission is required to use text recognition.")
    }
}

@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    onTextRecognized: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = androidx.camera.view.PreviewView(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Camera preview setup
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Image analysis setup for text recognition
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            cameraExecutor,
                            TextAnalyzer(onTextRecognized)
                        )
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Camera binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

// Text analyzer for handling text recognition
private class TextAnalyzer(private val onTextRecognized: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        // Recognize text
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                onTextRecognized(visionText.text)
            }
            .addOnFailureListener { e ->
                Log.e("TextAnalyzer", "Text recognition failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}