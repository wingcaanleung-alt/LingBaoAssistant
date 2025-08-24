package com.example.lingbao

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.*

class VoiceCommandService : Service(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private var recognizer: SpeechRecognizer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this)
            recognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.forEach { phrase ->
                        if (phrase.contains("灵宝")) {
                            val answer = AIAnalyzer.getAnswer(phrase)
                            tts.speak(answer, TextToSpeech.QUEUE_FLUSH, null, "LINGBAO_1")
                        }
                    }
                    // 识别结束后再次启动监听
                    startListening()
                }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    // 出错也重启监听
                    startListening()
                }
            })
            startListening()
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.SIMPLIFIED_CHINESE)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        recognizer?.startListening(intent)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.SIMPLIFIED_CHINESE
            tts.setPitch(1.0f)
            tts.setSpeechRate(0.95f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recognizer?.destroy()
        tts.stop()
        tts.shutdown()
    }
}
