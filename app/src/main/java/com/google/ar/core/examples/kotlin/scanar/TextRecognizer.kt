package com.google.ar.core.examples.kotlin.scanar

import android.media.Image
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognizer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun recognizeText(image: Image, rotationDegrees: Int, onResult: (String) -> Unit) {
        try {
            val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    onResult(visionText.text)
                }
                .addOnFailureListener { e ->
                    Log.e("TextRecognizer", "Text recognition failed: ${e.localizedMessage}", e)
                    onResult("Text recognition failed: ${e.localizedMessage}")
                }
                .addOnCompleteListener {
                    image.close() // Ensure the image is closed to prevent leaks
                }
        } catch (e: IllegalArgumentException) {
            Log.e("TextRecognizer", "Invalid image for text recognition: ${e.localizedMessage}", e)
            onResult("Invalid image for text recognition: ${e.localizedMessage}")
            image.close() // Ensure we close the image in case of an exception
        } catch (e: Exception) {
            Log.e("TextRecognizer", "Unexpected error: ${e.localizedMessage}", e)
            onResult("Unexpected error: ${e.localizedMessage}")
            image.close()
        }
    }
}