package com.easycd.nextplace.core.pleos.stt

import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.constant.ErrorType
import ai.pleos.playground.stt.constant.Mode
import ai.pleos.playground.stt.listener.OnInitializeListener
import ai.pleos.playground.stt.listener.OnServerConnectionListener
import ai.pleos.playground.stt.listener.ResultListener
import android.content.Context
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import com.easycd.nextplace.core.domain.sdk.SpeechRecognizer
import com.easycd.nextplace.core.domain.sdk.SttEvent
import com.easycd.nextplace.core.domain.sdk.SttFailure
import com.easycd.nextplace.core.pleos.audio.AudioFocusGate
import com.easycd.nextplace.core.pleos.audio.FocusLoss
import com.easycd.nextplace.core.pleos.system.PleosCredentials
import com.easycd.nextplace.core.pleos.system.PleosScope
import com.easycd.nextplace.core.pleos.system.connectWithRetry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * SpeechToText SDK 3.3.1 래퍼. 콜백을 Flow 로 바꾼다 (R7).
 * 자격 증명이 있으면 HYBRID(서버 → 실패 시 온디바이스), 없으면 ON_DEVICE.
 * 요청마다 리스너를 새로 붙였다가 떼므로, 끝난 요청의 늦은 콜백은 다음 요청에 섞이지 않는다 (기술서 7-0).
 */
@Singleton
class RealSpeechRecognizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val focus: AudioFocusGate,
    private val scope: PleosScope,
) : SpeechRecognizer {
    private val _status = MutableStateFlow(SdkStatus.CONNECTING)
    override val status: StateFlow<SdkStatus> = _status.asStateFlow()
    private var stt: SpeechToText? = null

    init {
        scope.connectWithRetry(_status) { connect() }
    }

    private suspend fun connect(): Boolean {
        stt?.runCatching { release() }
        val mode = if (PleosCredentials.present) Mode.HYBRID else Mode.ON_DEVICE
        val engine = SpeechToText(context, mode)
        val ok = suspendCancellableCoroutine { cont ->
            engine.initialize(object : OnInitializeListener {
                override fun onSuccess() { if (cont.isActive) cont.resume(true) }
                override fun onFailure(errorType: ErrorType) { if (cont.isActive) cont.resume(false) }
            })
        }
        if (!ok) return false
        engine.setLocale(Locale.KOREA)
        if (PleosCredentials.present) {
            engine.registerApp(PleosCredentials.clientId, PleosCredentials.clientSecret, object : OnServerConnectionListener {
                override fun onConnected() = Unit
                override fun onFailed(errorType: ErrorType) = Unit
            })
        }
        stt = engine
        return true
    }

    override fun listen(): Flow<SttEvent> = callbackFlow {
        val engine = stt
        if (engine == null || _status.value != SdkStatus.READY) {
            trySend(SttEvent.Failed(SttFailure.UNAVAILABLE))
            close()
            return@callbackFlow
        }
        val listener = object : ResultListener {
            override fun onReady() = Unit
            override fun onStartedRecognition() { trySend(SttEvent.Started) }
            override fun onUpdated(stt: String, completed: Boolean) {
                trySend(if (completed) SttEvent.Final(stt) else SttEvent.Partial(stt))
            }
            override fun onUpdatedEpdData(on: Long, off: Long) = Unit
            override fun onUpdatedRms(rms: Double) { trySend(SttEvent.Level(normalize(rms))) }
            override fun onEndedRecognition() { close() }
            override fun onError(errorType: ErrorType) {
                trySend(SttEvent.Failed(map(errorType)))
                close()
            }
        }
        focus.acquire()
        val lossJob = launch {
            focus.losses.collect { loss ->
                if (loss != FocusLoss.DUCK) {
                    trySend(SttEvent.Failed(SttFailure.FOCUS_LOST))
                    close()
                }
            }
        }
        engine.addListener(listener)
        engine.setLocale(Locale.KOREA)
        engine.request()
        awaitClose {
            lossJob.cancel()
            runCatching { engine.stop() }
            engine.removeListener(listener)
            focus.release()
        }
    }

    private fun normalize(rms: Double): Float = (rms / RMS_FULL).toFloat().coerceIn(0f, 1f)

    private fun map(e: ErrorType): SttFailure = when (e) {
        ErrorType.PERMISSION_DENIED -> SttFailure.PERMISSION
        ErrorType.TIMEOUT -> SttFailure.NO_SPEECH
        ErrorType.MIC_ERROR, ErrorType.AUDIO_ERROR, ErrorType.ON_DEVICE_ENGINE_ERROR, ErrorType.NOT_SUPPORTED_API -> SttFailure.UNAVAILABLE
        else -> SttFailure.OTHER
    }

    private companion object {
        const val RMS_FULL = 3_000.0
    }
}
