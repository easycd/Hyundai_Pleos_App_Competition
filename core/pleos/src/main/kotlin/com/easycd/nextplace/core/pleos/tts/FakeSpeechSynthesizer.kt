package com.easycd.nextplace.core.pleos.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.core.pleos.audio.AudioFocusGate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 가짜 TTS: Android 기본 TextToSpeech 로 읽는다. 엔진이 없으면 글자 수만큼 기다린다.
 * 읽은 문장은 [lastSpoken]으로 화면 구석(개발 표시)에 보인다.
 */
@Singleton
class FakeSpeechSynthesizer @Inject constructor(
    @ApplicationContext context: Context,
    private val texts: PromptTexts,
    private val focus: AudioFocusGate,
) : SpeechSynthesizer {
    override val status: StateFlow<SdkStatus> = MutableStateFlow(SdkStatus.READY)
    private val _speaking = MutableStateFlow(false)
    override val speaking: StateFlow<Boolean> = _speaking.asStateFlow()
    private val lock = Mutex()
    @Volatile private var ready = false
    @Volatile private var volume = 1f
    private val engine: TextToSpeech = TextToSpeech(context) { code ->
        ready = code == TextToSpeech.SUCCESS
        if (ready) runCatching { engineLocale() }
    }

    private val _lastSpoken = MutableStateFlow("")
    val lastSpoken: StateFlow<String> = _lastSpoken.asStateFlow()

    private fun engineLocale() {
        engine.language = Locale.KOREA
        engine.setAudioAttributes(focus.attributes)
    }

    override fun setVolume(volume: Float) {
        this.volume = volume
    }

    override suspend fun speak(prompt: VoicePrompt): Boolean = lock.withLock {
        val text = texts.text(prompt)
        _lastSpoken.value = text
        _speaking.value = true
        focus.acquire()
        try {
            if (!ready) {
                delay((text.length * MS_PER_CHAR).coerceAtMost(MAX_FAKE_MS))
                true
            } else {
                withTimeoutOrNull(MAX_SPEAK_MS) { speakAndWait(text) } ?: false
            }
        } finally {
            focus.release()
            _speaking.value = false
        }
    }

    private suspend fun speakAndWait(text: String): Boolean = suspendCancellableCoroutine { cont ->
        val id = UUID.randomUUID().toString()
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) { if (utteranceId == id && cont.isActive) cont.resume(true) }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { if (utteranceId == id && cont.isActive) cont.resume(false) }
            override fun onStop(utteranceId: String?, interrupted: Boolean) { if (utteranceId == id && cont.isActive) cont.resume(false) }
        })
        val params = Bundle().apply { putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume) }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, id)
        cont.invokeOnCancellation { engine.stop() }
    }

    override fun stop() {
        engine.stop()
    }

    private companion object {
        const val MS_PER_CHAR = 90L
        const val MAX_FAKE_MS = 5_000L
        const val MAX_SPEAK_MS = 15_000L
    }
}
