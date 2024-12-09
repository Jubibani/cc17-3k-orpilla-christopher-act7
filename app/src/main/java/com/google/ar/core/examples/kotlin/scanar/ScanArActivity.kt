package com.google.ar.core.examples.kotlin.scanar;

import android.media.Image
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper
import com.google.ar.core.examples.java.common.samplerender.*
import com.google.ar.core.examples.java.common.samplerender.Mesh
import com.google.ar.core.examples.java.common.samplerender.arcore.BackgroundRenderer
import com.google.ar.core.examples.kotlin.common.helpers.ARCoreSessionLifecycleHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.ar.core.examples.java.common.samplerender.SampleRender
import android.view.Surface
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.IOException
import com.google.ar.core.examples.java.common.samplerender.Shader

class ScanArActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ScanArActivity"
    }

    private lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
    private lateinit var renderer: Renderer
    private lateinit var surfaceView: GLSurfaceView
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private lateinit var sampleRender: SampleRender

    private val wordModelMap = mapOf(
//        "amphibians" to "library-model/frog.obj",
        "bacteria" to "library-model/bacteria.obj"
//        "platypus" to "library-model/platypus.obj",
//        "digestive" to "library-model/heart.obj",
//        "expose" to "library-model/heart.obj"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }

        arCoreSessionHelper = ARCoreSessionLifecycleHelper(this)
        arCoreSessionHelper.exceptionCallback = { exception ->
            val message = when (exception) {
                is UnavailableArcoreNotInstalledException -> "Please install ARCore"
                is UnavailableApkTooOldException -> "Please update ARCore"
                is UnavailableSdkTooOldException -> "Please update this app"
                is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
                else -> "Failed to create AR session: $exception"
            }
            Log.e(TAG, "ARCore threw an exception", exception)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        arCoreSessionHelper.beforeSessionResume = { session ->
            session.configure(
                session.config.apply {
                    focusMode = Config.FocusMode.AUTO
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                }
            )
        }

        lifecycle.addObserver(arCoreSessionHelper)

        surfaceView = GLSurfaceView(this)
        setContentView(surfaceView)
        renderer = Renderer(this)
        sampleRender = SampleRender(surfaceView, renderer, assets)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
    }

    inner class Renderer(val activity: ScanArActivity) : SampleRender.Renderer {
        private lateinit var backgroundRenderer: BackgroundRenderer
        private lateinit var virtualObjectShader: Shader
        private val virtualObjectMeshes = mutableMapOf<String, Mesh>()
        private val displayRotationHelper = DisplayRotationHelper(activity)
        private var hasSetTextureNames = false

        private var detectedWord: String? = null
        private var anchorMatrix = FloatArray(16)

        private lateinit var sampleRender: SampleRender

        override fun onSurfaceCreated(render: SampleRender) {
            sampleRender = render
            GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

            backgroundRenderer = BackgroundRenderer(sampleRender)
            virtualObjectShader = Shader.createFromAssets(
                sampleRender,
                "shaders/ar_unlit_object.vert",
                "shaders/ar_unlit_object.frag",
                null
            )

            wordModelMap.forEach { (word, modelPath) ->
                try {
                    virtualObjectMeshes[word] = Mesh.createFromAsset(sampleRender, modelPath)
                } catch (e: IOException) {
                    Log.e(TAG, "Error loading model for $word: ${e.localizedMessage}", e)
                }
            }
        }


        override fun onSurfaceChanged(render: SampleRender, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            displayRotationHelper.onSurfaceChanged(width, height)
        }

        override fun onDrawFrame(render: SampleRender) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

            val session = activity.arCoreSessionHelper.session ?: return
            displayRotationHelper.updateSessionIfNeeded(session)

            try {
                session.setCameraTextureName(backgroundRenderer.cameraColorTexture.textureId)
                val frame = session.update()
                backgroundRenderer.updateDisplayGeometry(frame)
                backgroundRenderer.drawBackground(render)

                // Perform text recognition
                val image = frame.acquireCameraImage()
                processImage(image)
                image.close()

                // Render AR objects
                if (detectedWord != null) {
                    val camera = frame.camera
                    val projectionMatrix = FloatArray(16)
                    camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)
                    val viewMatrix = FloatArray(16)
                    camera.getViewMatrix(viewMatrix, 0)

                    val mesh = virtualObjectMeshes[detectedWord]
                    if (mesh != null) {
                        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                        GLES20.glDepthMask(true)
                        virtualObjectShader.setMat4("u_ModelViewProjection", anchorMatrix)
                        virtualObjectShader.setVec4("u_Color", floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f))
                        render.draw(mesh, virtualObjectShader)
                    }
                }


            } catch (e: Exception) {
                Log.e(TAG, "Exception on the OpenGL thread", e)
            }
        }
        
        @RequiresApi(Build.VERSION_CODES.R)
        private fun getRotationDegrees(): Int {
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }

            val rotation = display?.rotation ?: Surface.ROTATION_0
            return when (rotation) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            }
        }

        private fun processImage(image: Image) {
            try {
                val buffer = image.planes[0].buffer
                val rotationDegrees = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    getRotationDegrees()
                } else {
                    // For older versions, use a default value or implement a fallback method
                    0
                }

                val inputImage = InputImage.fromByteBuffer(
                    buffer,
                    image.width,
                    image.height,
                    rotationDegrees,
                    InputImage.IMAGE_FORMAT_YUV_420_888 // Changed from NV21 to YUV_420_888
                )

                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val detectedText = visionText.text.lowercase()
                        for (word in wordModelMap.keys) {
                            if (word in detectedText) {
                                detectedWord = word
                                activity.runOnUiThread {
                                    Toast.makeText(activity, "Detected: $word", Toast.LENGTH_SHORT).show()
                                }
                                // Create an anchor at the center of the image
                                val session = activity.arCoreSessionHelper.session
                                val frame = session?.update()
                                val pose = frame?.camera?.pose
                                pose?.toMatrix(anchorMatrix, 0)
                                break
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Text recognition failed: ${e.localizedMessage}", e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image: ${e.localizedMessage}", e)
            }
        }
    }
}