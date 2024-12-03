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

        // Get the model path from the intent
        val modelPath = intent.getStringExtra("modelPath") ?: run {
            Log.e(TAG, "Model path not provided!")
            Toast.makeText(this, "No model selected!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Setup ARCore session lifecycle helper
        arCoreSessionHelper = ARCoreSessionLifecycleHelper(this)
        arCoreSessionHelper.exceptionCallback = { exception ->
            Log.e(TAG, "ARCore exception: ${exception.localizedMessage}")
            Toast.makeText(this, "ARCore error: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
        }
        arCoreSessionHelper.beforeSessionResume = ::configureSession
        lifecycle.addObserver(arCoreSessionHelper)

        // Set up AR rendering
        renderer = LibraryScanRenderer(this).apply {
            selectedModelPath = modelPath
        }
        lifecycle.addObserver(renderer)

        // Set up the AR view
        view = LibraryScanArView(this)
        lifecycle.addObserver(view)

        setContentView(view.root)
    }

    private fun configureSession(session: Session) {
        session.configure(
            session.config.apply {
                lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                depthMode = Config.DepthMode.AUTOMATIC
            }
        )
    }
}