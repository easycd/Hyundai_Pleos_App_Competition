package com.easycd.nextplace.core.pleos.tts

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import ai.pleos.playground.tts.listener.OnServerConnectionListener
import android.content.Context
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.core.pleos.audio.AudioFocusGate
import com.easycd.nextplace.core.pleos.audio.FocusLoss
import com.easycd.nextplace.core.pleos.system.PleosCredentials
import com.easycd.nextplace.core.pleos.system.PleosScope
import com.easycd.nextplace.core.pleos.system.connectWithRetry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * TextToSpeech SDK 3.3.1 래퍼. [speak]는 다 읽은 뒤(onDone) 돌아온다. 대답 대기는 이때부터 센다 (기술서 7-0).
 * SDK 에 음량 API 가 없어 음량 0 이면 읽지 않는다 (나머지 음량은 차량 음량을 따른다).
 */
@Singleton
class RealSpeechSynthesizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val texts: PromptTexts,
    private val focus: AudioFocusGate,
    scope: PleosScope,
) : SpeechSynthesizer {
    private val _status = MutableStateFlow(SdkStatus.CONNECTING)
    override val status: StateFlow<SdkStatus> = _status.asStateFlow()
    private val _speaking = MutableStateFlow(false)
    override val speaking: StateFlow<Boolean> = _speaking.asStateFlow()
    private var tts: TextToSpeech? = null
    private val lock = Mutex()
    @Volatile private var volume = 1f

    init {
        scope.connectWithRetry(_status) { connect() }
    }

    private suspend fun connect(): Boolean {
        tts?.runCatching { release() }
        val mode = if (PleosCredentials.present) Mode.HYBRID else Mode.ON_DEVICE
        val engine = TextToSpeech(context, mode)
        val ok = suspendCancellableCoroutine { cont ->
            // 시간 초과로 취소되면 답이 없던 엔진은 놓아 준다 (다음 시도가 새 엔진을 만든다)
            cont.invokeOnCancellation { runCatching { engine.release() } }
            engine.initialize(object : OnInitializeListener {
                override fun onSuccess() { if (cont.isActive) cont.resume(true) }
                override fun onFailure(errorType: ErrorType) { if (cont.isActive) cont.resume(false) }
            })
        }
        if (!ok) {
            runCatching { engine.release() }
            return false
        }
        engine.setLocale(Locale.KOREA)
        engine.setAudioAttributes(focus.attributes)
        if (PleosCredentials.present) {
            engine.registerApp(PleosCredentials.clientId, PleosCredentials.clientSecret, object : OnServerConnectionListener {
                override fun onConnected() = Unit
                override fun onFailed(errorType: ErrorType) = Unit
            })
        }
        tts = engine
        return true
    }

    /** TextToSpeech SDK 3.3.1 에는 음량 API 가 없다: 설정 화면은 켜기/끄기로 보인다. */
    override val supportsVolume: Boolean = false

    override fun setVolume(volume: Float) {
        this.volume = volume
    }

    override suspend fun speak(prompt: VoicePrompt): Boolean = lock.withLock {
        val engine = tts ?: return@withLock false
        if (volume <= 0f) return@withLock true
        val text = texts.text(prompt)
        // 포커스를 못 얻으면(통화 중 등) 읽지 않는다. 읽는 중에 잃으면(오리걸음 duck 제외) 멈춘다 (기술서 7-0)
        if (!focus.acquire()) return@withLock false
        _speaking.value = true
        try {
            withTimeoutOrNull(MAX_SPEAK_MS) {
                coroutineScope {
                    val lost = async { focus.losses.first { it != FocusLoss.DUCK } }
                    val spoken = async { speakAndWait(engine, text) }
                    select<Boolean> {
                        spoken.onAwait { it }
                        lost.onAwait { false }
                    }.also { lost.cancel(); spoken.cancel() }
                }
            } ?: false
        } finally {
            _speaking.value = false
            focus.release()
        }
    }

    private suspend fun speakAndWait(engine: TextToSpeech, text: String): Boolean = suspendCancellableCoroutine { cont ->
        val listener = object : EventListener {
            override fun onStart() = Unit
            override fun onReady() = Unit
            override fun onUpdatedRms(rms: Double) = Unit
            override fun onDone() = finish(true)
            override fun onStop() = finish(false)
            override fun onError(errorType: ErrorType) = finish(false)
            private fun finish(ok: Boolean) {
                engine.removeEventListener(this)
                if (cont.isActive) cont.resume(ok)
            }
        }
        engine.addEventListener(listener)
        engine.speak(text, true)
        cont.invokeOnCancellation {
            engine.removeEventListener(listener)
            runCatching { engine.stop() }
        }
    }

    override fun stop() {
        runCatching { tts?.stop() }
    }

    private companion object {
        const val MAX_SPEAK_MS = 15_000L
    }
}
