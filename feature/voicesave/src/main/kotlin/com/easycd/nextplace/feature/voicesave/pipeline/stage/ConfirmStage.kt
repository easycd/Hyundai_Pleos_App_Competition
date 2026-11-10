package com.easycd.nextplace.feature.voicesave.pipeline.stage

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.pipeline.ListenOutcome
import com.easycd.nextplace.core.domain.pipeline.ListenRequest
import com.easycd.nextplace.core.domain.pipeline.ListenStage
import com.easycd.nextplace.core.domain.pipeline.Stage
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.voice.Categories
import com.easycd.nextplace.core.domain.voice.Intent
import com.easycd.nextplace.core.domain.voice.IntentParser
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.core.domain.voice.VoiceStage
import com.easycd.nextplace.feature.voicesave.pipeline.ConfirmAnswer
import com.easycd.nextplace.feature.voicesave.pipeline.SaveFlowState
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import javax.inject.Inject

/** 확인 단계 입출력 (화면 상태 갱신·버튼 대답). */
interface ConfirmIO {
    fun show(state: SaveFlowState)
    val answers: ReceiveChannel<ConfirmAnswer>
}

data class ConfirmInput(val ranked: List<PoiCandidate>, val cues: Cues, val snapshot: LocationSnapshot, val io: ConfirmIO)

sealed interface ConfirmResult {
    data class Chosen(val candidate: PoiCandidate) : ConfirmResult
    data object Unconfirmed : ConfirmResult
    data object Cancelled : ConfirmResult
}

/**
 * 7단계 되묻기 (S-03, S-03b): 이름 넣은 TTS → 대기 5초(TTS 가 끝난 뒤부터) × 최대 2회.
 * 음성 대답과 버튼 중 먼저 온 하나만 받는다. 알아듣지 못한 말·무응답 = 미확인.
 */
class ConfirmStage @Inject constructor(
    private val tts: SpeechSynthesizer,
    private val listen: ListenStage,
    private val parser: IntentParser,
    private val clock: Clock,
) : Stage<ConfirmInput, ConfirmResult> {
    override val name = "7 Confirm"

    override suspend fun run(input: ConfirmInput): ConfirmResult {
        val asks = input.ranked.take(Tunables.CONFIRM_MAX_ASKS)
        for ((i, c) in asks.withIndex()) {
            while (input.io.answers.tryReceive().isSuccess) Unit
            val base = state(c, i == 1, input)
            input.io.show(base)
            tts.speak(if (i == 0) VoicePrompt.Confirm(c.name) else VoicePrompt.ConfirmSecond(c.name))
            input.io.show(base.copy(waitStartedAt = clock.now()))
            when (awaitReply(input.io)) {
                Reply.YES -> return ConfirmResult.Chosen(c)
                Reply.CANCEL -> return ConfirmResult.Cancelled
                Reply.NO -> continue
                Reply.NONE -> return ConfirmResult.Unconfirmed
            }
        }
        return ConfirmResult.Unconfirmed
    }

    private enum class Reply { YES, NO, CANCEL, NONE }

    /** 음성(TTS 뒤 5초 무응답이면 끝)과 버튼을 함께 기다리고 먼저 온 하나만 받는다. */
    private suspend fun awaitReply(io: ConfirmIO): Reply = coroutineScope {
        val voice = async {
            val out = listen.run(ListenRequest(maxMs = LISTEN_MAX_MS, noSpeechTimeoutMs = Tunables.CONFIRM_WAIT_MS))
            if (out !is ListenOutcome.Heard) return@async Reply.NONE
            when (parser.parse(out.text, VoiceStage.CONFIRM).intent) {
                Intent.YES -> Reply.YES
                Intent.NO -> Reply.NO
                Intent.CANCEL -> Reply.CANCEL
                else -> Reply.NONE
            }
        }
        val reply = select {
            voice.onAwait { it }
            io.answers.onReceive { if (it == ConfirmAnswer.YES) Reply.YES else Reply.NO }
        }
        voice.cancel()
        reply
    }

    private fun state(c: PoiCandidate, second: Boolean, input: ConfirmInput): SaveFlowState.Confirming {
        val spoken = input.snapshot.current
        val point = GeoPoint(c.lat, c.lon)
        val distance = spoken?.let { Geo.distanceM(it.point, point).toInt() } ?: c.distanceM.toInt()
        val ahead = spoken?.let { Geo.isAhead(it.point, it.heading.toDouble(), point) } ?: false
        val label = Categories.all.firstOrNull { Categories.matches(it, c.category) }?.name
            ?: c.category?.substringAfterLast(" > ")
        return SaveFlowState.Confirming(c, second, label, distance, ahead, null)
    }

    private companion object {
        const val LISTEN_MAX_MS = 9_000L
    }
}
