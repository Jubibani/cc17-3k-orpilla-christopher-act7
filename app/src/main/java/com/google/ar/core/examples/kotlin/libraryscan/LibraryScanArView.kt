package com.google.ar.core.examples.kotlin.libraryscan

import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.ar.core.examples.java.common.helpers.TapHelper

class LibraryScanArView(
    private val activity: LibraryScanActivity
) : DefaultLifecycleObserver {

    val root = View.inflate(activity, R.layout.activity_main, null)
    val arFragment = activity.supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment

    val surfaceView = arFragment.arSceneView
    val tapHelper = TapHelper(activity).also {
        surfaceView.setOnTouchListener(it)
    }

    val scene = surfaceView.scene

}