package com.hichamdzz.translator.service

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.hichamdzz.translator.MainActivity
import com.hichamdzz.translator.R
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

class FloatingWidgetService : Service() {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var expandedView: View? = null
    private var isExpanded = false
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private var sourceLang = "ar"
    private var targetLang = "en"
    private var sourceName = "عربي"
    private var targetName = "English"

    private val languages = listOf(
        Triple("ar", "عربي", "🇸🇦"), Triple("en", "English", "🇺🇸"),
        Triple("fr", "Français", "🇫🇷"), Triple("es", "Español", "🇪🇸"),
        Triple("de", "Deutsch", "🇩🇪"), Triple("tr", "Türkçe", "🇹🇷"),
        Triple("ru", "Русский", "🇷🇺"), Triple("zh", "中文", "🇨🇳"),
    )
    private var sourceIdx = 0
    private var targetIdx = 1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "floating_channel")
            .setContentTitle("Translated by Hisham")
            .setContentText("الزر العائم نشط")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true).build()
        startForeground(2001, notification)

        tts = TextToSpeech(this) { if (it == TextToSpeech.SUCCESS) ttsReady = true }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupFloatingButton()
    }

    private fun setupFloatingButton() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget_layout, null)
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 50; y = 300 }

        windowManager?.addView(floatingView, params)

        var initX = 0; var initY = 0; var touchX = 0f; var touchY = 0f
        floatingView?.findViewById<ImageView>(R.id.floating_icon)?.setOnTouchListener { _, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> { initX = params.x; initY = params.y; touchX = ev.rawX; touchY = ev.rawY; true }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initX + (ev.rawX - touchX).toInt()
                    params.y = initY + (ev.rawY - touchY).toInt()
                    windowManager?.updateViewLayout(floatingView, params); true
                }
                MotionEvent.ACTION_UP -> {
                    if (Math.abs(ev.rawX - touchX) < 15 && Math.abs(ev.rawY - touchY) < 15) toggleExpanded()
                    true
                }
                else -> false
            }
        }
    }

    private fun toggleExpanded() {
        if (isExpanded) { hideExpanded() } else { showExpanded() }
        isExpanded = !isExpanded
    }

    private fun showExpanded() {
        expandedView = LayoutInflater.from(this).inflate(R.layout.floating_expanded_layout, null)
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        windowManager?.addView(expandedView, params)

        expandedView?.findViewById<Button>(R.id.btn_translate)?.setOnClickListener { startListeningAndTranslate() }
        expandedView?.findViewById<Button>(R.id.btn_swap_lang)?.setOnClickListener { swapLanguages() }
        expandedView?.findViewById<Button>(R.id.btn_my_lang)?.setOnClickListener { cycleSourceLang() }
        expandedView?.findViewById<Button>(R.id.btn_their_lang)?.setOnClickListener { cycleTargetLang() }
        expandedView?.findViewById<Button>(R.id.btn_close)?.setOnClickListener { toggleExpanded() }

        updateLangButtons()
    }

    private fun hideExpanded() {
        expandedView?.let { windowManager?.removeView(it) }
        expandedView = null
    }

    private fun updateLangButtons() {
        expandedView?.findViewById<Button>(R.id.btn_my_lang)?.text = "لغتي: $sourceName"
        expandedView?.findViewById<Button>(R.id.btn_their_lang)?.text = "لغتهم: $targetName"
    }

    private fun cycleSourceLang() {
        sourceIdx = (sourceIdx + 1) % languages.size
        if (sourceIdx == targetIdx) sourceIdx = (sourceIdx + 1) % languages.size
        sourceLang = languages[sourceIdx].first; sourceName = languages[sourceIdx].second
        updateLangButtons()
    }

    private fun cycleTargetLang() {
        targetIdx = (targetIdx + 1) % languages.size
        if (targetIdx == sourceIdx) targetIdx = (targetIdx + 1) % languages.size
        targetLang = languages[targetIdx].first; targetName = languages[targetIdx].second
        updateLangButtons()
    }

    private fun swapLanguages() {
        val tmpI = sourceIdx; sourceIdx = targetIdx; targetIdx = tmpI
        sourceLang = languages[sourceIdx].first; sourceName = languages[sourceIdx].second
        targetLang = languages[targetIdx].first; targetName = languages[targetIdx].second
        updateLangButtons()
    }

    private fun startListeningAndTranslate() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            expandedView?.findViewById<TextView>(R.id.tv_recognized)?.text = "❌ التعرف غير متاح"
            return
        }
        expandedView?.findViewById<TextView>(R.id.tv_recognized)?.text = "🎤 جارٍ الاستماع..."
        expandedView?.findViewById<Button>(R.id.btn_translate)?.text = "⏳ يستمع..."

        val recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLang)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: ""
                expandedView?.findViewById<TextView>(R.id.tv_recognized)?.text = "🎤 $text"
                expandedView?.findViewById<Button>(R.id.btn_translate)?.text = "🎤 ترجم"
                recognizer.destroy()
                Thread { translateAndSpeak(text) }.start()
            }
            override fun onError(error: Int) {
                expandedView?.findViewById<TextView>(R.id.tv_recognized)?.text = "❌ خطأ: $error"
                expandedView?.findViewById<Button>(R.id.btn_translate)?.text = "🎤 ترجم"
                recognizer.destroy()
            }
            override fun onPartialResults(p: Bundle?) {
                val t = p?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (t != null) expandedView?.findViewById<TextView>(R.id.tv_recognized)?.text = "🎤 $t..."
            }
            override fun onReadyForSpeech(p: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(r: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(t: Int, p: Bundle?) {}
        })
        recognizer.startListening(intent)
    }

    private fun translateAndSpeak(text: String) {
        try {
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceLang&tl=$targetLang&dt=t&q=${URLEncoder.encode(text, "UTF-8")}"
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            val resp = conn.inputStream.bufferedReader().readText()
            val translated = resp.substringAfter("\"").substringBefore("\"")

            Handler(Looper.getMainLooper()).post {
                expandedView?.findViewById<TextView>(R.id.tv_translated)?.text = "🌐 $translated"
            }

            if (ttsReady) {
                tts?.language = Locale(targetLang)
                tts?.speak(translated, TextToSpeech.QUEUE_FLUSH, null, "float_tts")
            }
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                expandedView?.findViewById<TextView>(R.id.tv_translated)?.text = "❌ ${e.message}"
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel("floating_channel", "Floating Widget", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager?.removeView(it) }
        expandedView?.let { windowManager?.removeView(it) }
        tts?.shutdown()
    }
}
