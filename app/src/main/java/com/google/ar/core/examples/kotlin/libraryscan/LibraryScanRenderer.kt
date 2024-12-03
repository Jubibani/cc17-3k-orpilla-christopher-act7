package com.google.ar.core.examples.kotlin.libraryscan

import android.net.Uri
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.core.examples.java.common.samplerender.SampleRender
import com.google.ar.sceneform.rendering.ModelRenderable

class LibraryScanRenderer(
    private val activity: LibraryScanActivity
) : SampleRender.Renderer, DefaultLifecycleObserver { // Implement DefaultLifecycleObserver

    var selectedModelPath: String? = null // Path for the selected model

    override fun onSurfaceCreated(render: SampleRender) {
        Log.d(TAG, "Surface created")
        // You can initialize rendering resources here if needed
    }

    override fun onSurfaceChanged(render: SampleRender, width: Int, height: Int) {
        Log.d(TAG, "Surface changed: width=$width, height=$height")
        // Handle changes to the rendering surface (e.g., screen rotation)
    }

    override fun onDrawFrame(render: SampleRender) {
        val session = activity.arCoreSessionHelper.session ?: return
        val frame = session.update()
        val camera = frame.camera

        // Handle taps for placing models
        handleTap(frame)
    }

    private fun handleTap(frame: Frame) {
        if (frame.camera.trackingState != TrackingState.TRACKING) return

        val tap = activity.view.tapHelper.poll() ?: return

        val hitResult = frame.hitTest(tap).firstOrNull { hit ->
            val trackable = hit.trackable
            trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)
        } ?: return

        val modelPath = selectedModelPath ?: run {
            Log.e(TAG, "No model path provided!")
            return
        }

        // Load and render the model
        ModelRenderable.builder()
            .setSource(activity, Uri.parse(modelPath))
            .build()
            .thenAccept { renderable ->
                val anchor = hitResult.createAnchor()
                val anchorNode = com.google.ar.sceneform.AnchorNode(anchor).apply {
                    this.renderable = renderable
                }
                activity.view.scene.addChild(anchorNode)
            }
            .exceptionally { throwable ->
                Log.e(TAG, "Error loading model: ${throwable.localizedMessage}")
                null
            }
    }

    override fun onResume(owner: LifecycleOwner) {
        Log.d(TAG, "Renderer resumed")
    }

    override fun onPause(owner: LifecycleOwner) {
        Log.d(TAG, "Renderer paused")
    }

    companion object {
        private const val TAG = "LibraryScanRenderer"
    }
}