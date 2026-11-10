package com.easycd.nextplace.core.pleos.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 포커스를 잃은 종류 (기술서 7-0). */
enum class FocusLoss { TRANSIENT, PERMANENT, DUCK }

/**
 * 오디오 포커스 (기술서 7-0, 기획서 10장 19번). TTS·STT 전에 GAIN_TRANSIENT_MAY_DUCK 을 요청한다.
 * usage 는 Interaction Matrix 확인 전 가정으로 ASSISTANT. 잃으면 [losses]로 알린다.
 */
@Singleton
class AudioFocusGate @Inject constructor(@ApplicationContext context: Context) {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val _losses = MutableSharedFlow<FocusLoss>(extraBufferCapacity = 4)
    val losses: SharedFlow<FocusLoss> = _losses.asSharedFlow()
    private var holders = 0

    val attributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANT)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(attributes)
        .setOnAudioFocusChangeListener { change ->
            when (change) {
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> _losses.tryEmit(FocusLoss.TRANSIENT)
                AudioManager.AUDIOFOCUS_LOSS -> _losses.tryEmit(FocusLoss.PERMANENT)
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> _losses.tryEmit(FocusLoss.DUCK)
            }
        }
        .build()

    @Synchronized
    fun acquire(): Boolean {
        holders++
        if (holders > 1) return true
        return audio?.requestAudioFocus(request) != AudioManager.AUDIOFOCUS_REQUEST_FAILED
    }

    @Synchronized
    fun release() {
        holders = (holders - 1).coerceAtLeast(0)
        if (holders == 0) audio?.abandonAudioFocusRequest(request)
    }
}
