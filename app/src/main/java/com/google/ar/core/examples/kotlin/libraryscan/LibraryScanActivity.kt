package com.google.ar.core.examples.kotlin.libraryscan

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.examples.kotlin.common.helpers.ARCoreSessionLifecycleHelper

class LibraryScanActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "LibraryScanActivity"
    }

    lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
    lateinit var view: LibraryScanArView
    lateinit var renderer: LibraryScanRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get selected model path from Intent
        val modelPath: String = intent.getStringExtra("modelPath") ?: run {
            Log.e(TAG, "No model path provided!")
            Toast.makeText(this, "No model selected!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Setup ARCore session lifecycle helper
        arCoreSessionHelper = ARCoreSessionLifecycleHelper(this).apply {
            beforeSessionResume = ::configureSession
        }
        lifecycle.addObserver(arCoreSessionHelper)

        // Setup renderer and pass selected model path
        renderer = LibraryScanRenderer(this).apply {
            selectedModelPath = modelPath
        }
        lifecycle.addObserver(renderer)

        // Setup AR view
        view = LibraryScanArView(this)
        lifecycle.addObserver(view)

        setContentView(view.root)
    }

    private fun configureSession(session: Session) {
        session.configure(session.config.apply {
            lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            depthMode = Config.DepthMode.AUTOMATIC
        })
    }
}