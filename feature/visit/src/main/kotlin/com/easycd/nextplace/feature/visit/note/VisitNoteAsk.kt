package com.easycd.nextplace.feature.visit.note

import android.content.Context
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.Messages
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.domain.pipeline.ListenOutcome
import com.easycd.nextplace.core.domain.pipeline.ListenRequest
import com.easycd.nextplace.core.domain.pipeline.ListenStage
import com.easycd.nextplace.core.domain.repository.VisitRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.sdk.SttEvent
import com.easycd.nextplace.core.domain.voice.Intent
import com.easycd.nextplace.core.domain.voice.IntentParser
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.core.domain.voice.VoiceSession
import com.easycd.nextplace.core.domain.voice.VoiceStage
import com.easycd.nextplace.core.ui.notify.HeadsUp
import com.easycd.nextplace.feature.visit.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * S-15 방문 메모 질문 (기술서 7-4 3): 다른 음성 대화가 끝날 때까지 줄을 서고, TTS "…다녀오셨네요. 한 줄 남길까요?"
 * 뒤 음성 5초 · 버튼 10초. "됐어"·"괜찮아요"는 메모 없이 닫고, 그 밖의 문장은 모두 이 방문의 메모로 받는다.
 */
@Singleton
class VisitNoteAsk @Inject constructor(
    @ApplicationContext private val context: Context,
    private val voice: VoiceSession,
    private val tts: SpeechSynthesizer,
    private val listen: ListenStage,
    private val parser: IntentParser,
    private val visits: VisitRepository,
    private val messages: Messages,
    private val headsUp: HeadsUp,
    private val clock: Clock,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow<VisitNoteState>(VisitNoteState.Hidden)
    val state: StateFlow<VisitNoteState> = _state.asStateFlow()
    private var button: CompletableDeferred<Boolean>? = null

    fun ask(visitId: String, placeName: String) {
        scope.launch {
            voice.runQueued(VoiceSession.Kind.VISIT_NOTE, QUEUE_MAX_MS) { runAsk(visitId, placeName) }
        }
    }

    /** 버튼: true = 말하기(바로 듣기), false = 다음에. */
    fun press(speak: Boolean) {
        button?.complete(speak)
    }

    private suspend fun runAsk(visitId: String, placeName: String) {
        val count = visits.get(visitId)?.placeId?.let { countFor(it) } ?: 1
        _state.value = VisitNoteState.Asking(placeName, count, null, false, "")
        headsUp.show(HeadsUp.VISIT_ID, context.getString(R.string.visit_title, placeName), context.getString(R.string.visit_notify_text), Tunables.REMIND_BUTTON_WAIT_MS * 2)
        try {
            button = CompletableDeferred()
            tts.speak(VoicePrompt.VisitAsk(placeName))
            update { it.copy(waitStartedAt = clock.now(), listening = true) }
            val memo = awaitMemo() ?: return
            visits.setNote(visitId, memo)
            messages.show(UserMessage.NoteSaved(placeName, memo))
            tts.speak(VoicePrompt.NoteSaved(placeName))
        } finally {
            button = null
            headsUp.cancel(HeadsUp.VISIT_ID)
            _state.value = VisitNoteState.Hidden
        }
    }

    private suspend fun awaitMemo(): String? = coroutineScope {
        val voiceAnswer = async { listenMemo(Tunables.REMIND_VOICE_WAIT_MS) }
        val pressed = button!!
        val first = select<Any?> {
            voiceAnswer.onAwait { it }
            pressed.onAwait { it }
        }
        when (first) {
            is String -> first
            true -> {
                voiceAnswer.cancel()
                listenMemo(Tunables.REMIND_VOICE_WAIT_MS)
            }
            false -> {
                voiceAnswer.cancel()
                null
            }
            else -> withTimeoutOrNull(Tunables.REMIND_BUTTON_WAIT_MS - Tunables.REMIND_VOICE_WAIT_MS) { pressed.await() }
                ?.let { if (it) listenMemo(Tunables.REMIND_VOICE_WAIT_MS) else null }
        }
    }

    private suspend fun listenMemo(noSpeechMs: Long): String? {
        val out = listen.run(ListenRequest(noSpeechTimeoutMs = noSpeechMs, progress = { e -> if (e is SttEvent.Partial) update { it.copy(partial = e.text) } }))
        val text = (out as? ListenOutcome.Heard)?.text ?: return null
        return if (parser.parse(text, VoiceStage.MEMO).intent == Intent.MEMO) text else null
    }

    private suspend fun countFor(placeId: String): Int = visits.countFor(placeId)

    private fun update(f: (VisitNoteState.Asking) -> VisitNoteState.Asking) {
        val s = _state.value as? VisitNoteState.Asking ?: return
        _state.value = f(s)
    }

    private companion object {
        const val QUEUE_MAX_MS = 10 * 60_000L
    }
}
