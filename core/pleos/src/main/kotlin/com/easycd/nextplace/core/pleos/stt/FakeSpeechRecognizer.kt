package com.easycd.nextplace.core.pleos.stt

import com.easycd.nextplace.core.domain.sdk.DevVoiceInput
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import com.easycd.nextplace.core.domain.sdk.SpeechRecognizer
import com.easycd.nextplace.core.domain.sdk.SttEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 가짜 STT (기술서 10장: Emulator 에서 SDK 가 안 될 때 개발 빌드가 쓰는 대체).
 * 듣는 동안 화면의 '가짜 음성 입력' 칸에 적은 문장을 한 단어씩 부분 결과로 흘린 뒤 최종 결과로 낸다.
 */
@Singleton
class FakeSpeechRecognizer @Inject constructor() : SpeechRecognizer, DevVoiceInput {
    override val status: StateFlow<SdkStatus> = MutableStateFlow(SdkStatus.READY)
    override val enabled: Boolean = true
    private val typed = MutableSharedFlow<String>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _listening = MutableStateFlow(false)
    override val listening: StateFlow<Boolean> = _listening

    override fun submit(text: String) {
        typed.tryEmit(text)
    }

    override fun listen(): Flow<SttEvent> = flow {
        _listening.value = true
        try {
            emit(SttEvent.Started)
            val text = typed.first()
            val words = text.trim().split(' ').filter { it.isNotEmpty() }
            val sb = StringBuilder()
            words.forEachIndexed { i, w ->
                if (i > 0) sb.append(' ')
                sb.append(w)
                emit(SttEvent.Level(LEVELS[i % LEVELS.size]))
                emit(SttEvent.Partial(sb.toString()))
                delay(WORD_MS)
            }
            emit(SttEvent.Final(sb.toString()))
        } finally {
            _listening.value = false
        }
    }

    private companion object {
        const val WORD_MS = 120L
        val LEVELS = listOf(0.4f, 0.8f, 0.6f, 1f, 0.5f)
    }
}
