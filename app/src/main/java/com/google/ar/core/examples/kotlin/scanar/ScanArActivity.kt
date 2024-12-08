package com.google.ar.core.examples.kotlin.helloar;
/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import ScanArView
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.Config
import com.google.ar.core.Config.InstantPlacementMode
import com.google.ar.core.Session
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper
import com.google.ar.core.examples.java.common.helpers.DepthSettings
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper
import com.google.ar.core.examples.java.common.helpers.InstantPlacementSettings
import com.google.ar.core.examples.java.common.samplerender.SampleRender
import com.google.ar.core.examples.kotlin.common.helpers.ARCoreSessionLifecycleHelper
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.core.Frame
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.camera.core.Preview
import android.util.Size
import androidx.camera.camera2.internal.annotation.CameraExecutor
import androidx.camera.core.AspectRatio
import java.util.Locale
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Camera
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import com.google.mlkit.vision.text.TextRecognizer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3D model.
 */
class ScanArActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "HelloArActivity"
    }

    lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
    lateinit var view: ScanArView
    private lateinit var renderer: ScanArRenderer

    val instantPlacementSettings = InstantPlacementSettings()
    val depthSettings = DepthSettings()

    //for process frames
    private lateinit var displayRotationHelper: DisplayRotationHelper
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    //refining text recognition
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var cameraExecutor: ExecutorService

    private var lastProcessingTime = 0L
    private val processingInterval = 1000 // 1 second interval
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the renderer and pass 'this' as the activity
        renderer = ScanArRenderer(this)
        // Setup ARCore session lifecycle helper and configuration.
        arCoreSessionHelper = ARCoreSessionLifecycleHelper(this)
        // If Session creation or Session.resume() fails, display a message and log detailed
        // information.
        arCoreSessionHelper.exceptionCallback =
            { exception ->
                val message =
                    when (exception) {
                        is UnavailableUserDeclinedInstallationException ->
                            "Please install Google Play Services for AR"
                        is UnavailableApkTooOldException -> "Please update ARCore"
                        is UnavailableSdkTooOldException -> "Please update this app"
                        is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
                        is CameraNotAvailableException -> "Camera not available. Try restarting the app."
                        else -> "Failed to create AR session: $exception"
                    }
                Log.e(TAG, "ARCore threw an exception", exception)
                view.snackbarHelper.showError(this, message)
            }
        //


        // Configure session features, including: Lighting Estimation, Depth mode, Instant Placement.
        arCoreSessionHelper.beforeSessionResume = ::configureSession
        lifecycle.addObserver(arCoreSessionHelper)

        // Set up the Hello AR renderer.
        renderer = ScanArRenderer(this)
        lifecycle.addObserver(renderer)

        // Set up Hello AR UI.
        view = ScanArView(this)
        lifecycle.addObserver(view)
        setContentView(view.root)

        // Sets up an example renderer using our HelloARRenderer.
        SampleRender(view.surfaceView, renderer, assets)

        depthSettings.onCreate(this)
        instantPlacementSettings.onCreate(this)

        //for process frames
        displayRotationHelper = DisplayRotationHelper(this)

        //initialize camera
        cameraExecutor = Executors.newSingleThreadExecutor()

        //for refining text recognition
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(this))


    }


    /**
     * Handle the recognized text and update the UI.
     */

    // Configure the session, using Lighting Estimation, and Depth mode.
    fun configureSession(session: Session) {
        session.configure(
            session.config.apply {
                lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR

                // Depth API is used if it is configured in Hello AR's settings.
                depthMode =
                    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                        Config.DepthMode.AUTOMATIC
                    } else {
                        Config.DepthMode.DISABLED
                    }

                // Instant Placement is used if it is configured in Hello AR's settings.
                instantPlacementMode =
                    if (instantPlacementSettings.isInstantPlacementEnabled) {
                        InstantPlacementMode.LOCAL_Y_UP
                    } else {
                        InstantPlacementMode.DISABLED
                    }
            }
        )
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            // Use toast instead of snackbar here since the activity will exit.
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
    }



    //processing frames for text recognition
    private val targetWords = listOf("platypus", "bacteria", "digestive", "amphibian", "heart")

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun processImageForTextRecognition(mediaImage: Image, rotationDegrees: Int) {
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val recognizedText = visionText.text.lowercase(Locale.getDefault())
                Log.d(TAG, "Recognized text: $recognizedText")
                val detectedWords = targetWords.filter { it in recognizedText }
                if (detectedWords.isNotEmpty()) {
                    val message = "Detected: ${detectedWords.joinToString(", ")}"
                    handleRecognizedText(message)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Text recognition failed: ${e.localizedMessage}", e)
            }
    }


    //Text Recognition
    private var lastToastTime = 0L

    private fun handleRecognizedText(text: String) {
        runOnUiThread {
            val textView = findViewById<TextView>(R.id.recognizedTextView)
            textView.text = text
            textView.visibility = View.VISIBLE

            // Hide the TextView after 2 seconds
            textView.postDelayed({ textView.visibility = View.GONE }, 2000)
        }
    }

    //refining TextRecognition
    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        try {
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val preview = Preview.Builder().build()

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor, TextRecognitionAnalyzer(targetWords) { recognizedText ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastProcessingTime >= processingInterval) {
                    handleRecognizedText(recognizedText)
                    lastProcessingTime = currentTime
                }
            })

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this as LifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )

        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }



    private fun processRecognizedText(recognizedText: String) {
        // Process the recognized text here
        // You can call your existing processFrame method or implement new logic
        val detectedWords = targetWords.filter { it in recognizedText.lowercase() }
        if (detectedWords.isNotEmpty()) {
            val message = "Detected: ${detectedWords.joinToString(", ")}"
            handleRecognizedText(message)
        }
    }


}

//for refining text recognition
private class TextRecognitionAnalyzer(
    private val targetWords: List<String>,
    private val onTextRecognized: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastProcessedTimestamp: Long = 0

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastProcessedTimestamp < 500) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val recognizedText = visionText.text.lowercase()
                    val detectedWords = targetWords.filter { it in recognizedText }
                    if (detectedWords.isNotEmpty()) {
                        val message = "Detected: ${detectedWords.joinToString(", ")}"
                        onTextRecognized(message)
                    }
                    lastProcessedTimestamp = currentTimestamp
                }
                .addOnFailureListener { e ->
                    Log.e("TextRecognitionAnalyzer", "Text recognition failed: ${e.message}", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}