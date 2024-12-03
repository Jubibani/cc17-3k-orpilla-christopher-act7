package com.google.ar.core.examples.kotlin.libraryscan

import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.ar.core.examples.java.common.helpers.TapHelper
import com.google.ar.sceneform.ux.ArFragment

class LibraryScanArView(
    private val activity: LibraryScanActivity
) : DefaultLifecycleObserver { // Implement DefaultLifecycleObserver

    val root = View.inflate(activity, R.layout.activity_library_scan, null)
    val arFragment = activity.supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment

    val surfaceView = arFragment.arSceneView // The AR scene view
    val tapHelper = TapHelper(activity).also {
        surfaceView.setOnTouchListener(it) // Set touch listener for taps
    }

    val scene = surfaceView.scene // Expose the scene for rendering


}