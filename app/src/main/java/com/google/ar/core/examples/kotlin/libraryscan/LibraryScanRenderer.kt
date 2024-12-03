package com.google.ar.core.examples.kotlin.libraryscan

import android.net.Uri
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.rendering.ModelRenderable

class LibraryScanRenderer(
    private val activity: LibraryScanActivity
) : DefaultLifecycleObserver {

    var selectedModelPath: String? = null

    fun onDrawFrame(render: com.google.ar.core.examples.java.common.samplerender.SampleRender) {
        val session = activity.arCoreSessionHelper.session ?: return
        val frame = session.update()
        val camera = frame.camera

        // Handle user tap to place model
        handleTap(frame)
    }

    private fun handleTap(frame: Frame) {
        if (frame.camera.trackingState != TrackingState.TRACKING) return

        val tap = activity.view.tapHelper.poll() ?: return

        val hitResult = frame.hitTest(tap).firstOrNull { hit ->
            val trackable = hit.trackable
            trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)
        } ?: return

        val modelPath = selectedModelPath ?: return
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
            .exceptionally {
                Log.e("LibraryScanRenderer", "Error loading model: ${it.localizedMessage}")
                null
            }
    }
}