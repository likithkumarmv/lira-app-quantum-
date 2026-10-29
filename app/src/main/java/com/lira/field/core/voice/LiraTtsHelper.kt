package com.lira.field.core.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class LiraTtsHelper(
    private val context: Context,
    private val onInitListener: ((Boolean) -> Unit)? = null
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    var isInitialized = false
        private set

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("LiraTtsHelper", "Failed to initialize TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            onInitListener?.invoke(true)
        } else {
            isInitialized = false
            onInitListener?.invoke(false)
        }
    }

    fun isLanguageSupported(locale: Locale): Boolean {
        val engine = tts ?: return false
        val res = engine.isLanguageAvailable(locale)
        return res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun speak(
        text: String,
        lang: String,
        onVoiceMissing: (() -> Unit)? = null
    ) {
        val engine = tts ?: return
        val targetLocale = resolveLocale(lang, { isLanguageSupported(it) }, onVoiceMissing)
        engine.language = targetLocale
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LIRA_DISPATCH_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }

    companion object {
        fun getLocaleForLanguage(lang: String): Locale {
            return when (lang.lowercase(Locale.ROOT)) {
                "hi" -> Locale("hi", "IN")
                "mr" -> Locale("mr", "IN")
                "ta" -> Locale("ta", "IN")
                "kn" -> Locale("kn", "IN")
                "te" -> Locale("te", "IN")
                else -> Locale.ENGLISH
            }
        }

        fun resolveLocale(
            lang: String,
            isSupported: (Locale) -> Boolean,
            onVoiceMissing: (() -> Unit)? = null
        ): Locale {
            val targetLocale = getLocaleForLanguage(lang)
            return if (isSupported(targetLocale)) {
                targetLocale
            } else {
                if (lang.lowercase(Locale.ROOT) != "en") {
                    onVoiceMissing?.invoke()
                }
                Locale.ENGLISH
            }
        }
    }
}
