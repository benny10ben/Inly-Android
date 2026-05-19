package com.ben.inly.domain.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import java.util.Locale

class NativeVoiceRecognizer(private val context: Context) : VoiceRecognizer {
    private var speechRecognizer: SpeechRecognizer? = null

    override fun startListening(
        onPartial: (String) -> Unit,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        Handler(Looper.getMainLooper()).post {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                onError("Microphone permission missing. Please enable it in system settings.")
                return@post
            }

            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                onError("Speech recognition framework is unavailable on this hardware target.")
                return@post
            }

            destroyInternal()

            val isStrictOfflineAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

            speechRecognizer = if (isStrictOfflineAvailable) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording failure"
                        SpeechRecognizer.ERROR_CLIENT -> "Client binding lost"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions rejected by OS"
                        SpeechRecognizer.ERROR_NETWORK -> "Network access required for this engine"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network operational timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try speaking again."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice subsystem is busy. Wait a moment."
                        SpeechRecognizer.ERROR_SERVER -> "Server processing exception dropped"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Timed out."
                        10 -> "Local language pack missing. Please check your device offline settings."
                        else -> "Speech subsystem exception code: $error"
                    }
                    onError(message)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onResult(matches[0])
                    } else {
                        onError("No text extracted from vocal sequence")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onPartial(matches[0])
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
            }

            try {
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                onError("Failed to start speech pipeline: ${e.message}")
            }
        }
    }

    override fun stopListening() {
        Handler(Looper.getMainLooper()).post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
            }
        }
    }

    private fun destroyInternal() {
        speechRecognizer?.let {
            try {
                it.stopListening()
                it.cancel()
                it.destroy()
            } catch (e: Exception) {
            }
            speechRecognizer = null
        }
    }

    override fun destroy() {
        Handler(Looper.getMainLooper()).post {
            destroyInternal()
        }
    }
}