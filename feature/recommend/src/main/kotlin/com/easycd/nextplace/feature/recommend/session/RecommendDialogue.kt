package com.easycd.nextplace.feature.recommend.session

import com.easycd.nextplace.core.domain.pipeline.ListenOutcome
import com.easycd.nextplace.core.domain.pipeline.ListenRequest
import com.easycd.nextplace.core.domain.pipeline.ListenStage
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.voice.Intent
import com.easycd.nextplace.core.domain.voice.IntentParser
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.core.domain.voice.VoiceStage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 추천 음성 대화: 번호와 이름을 읽고(읽는 카드 강조) 추천 단계(SELECT · NEXT · CANCEL)로 듣는다.
 * 없는 번호면 "1번부터 n번 중에 골라 주세요" 1회, 같은 이름이 둘이면 "몇 번이요?" 1회, 그 밖의 말이면 "번호로 말해 주세요" 1회.
 */
class RecommendDialogue @Inject constructor(
    private val tts: SpeechSynthesizer,
    private val listen: ListenStage,
    private val parser: IntentParser,
) {
    suspend fun run(
        cards: List<RecCard>,
        onReading: (Int?) -> Unit,
        onSelect: (Int) -> Unit,
        onNext: () -> Unit,
        onCancel: () -> Unit,
        /** false: 목록을 다시 읽지 않고 바로 듣는다 (선택을 1.5초 안에 취소하고 돌아왔을 때). */
        readList: Boolean = true,
    ) {
        if (readList) coroutineScope {
            val highlight = launch {
                cards.indices.forEach { i ->
                    onReading(i)
                    delay(PER_ITEM_MS)
                }
            }
            tts.speak(VoicePrompt.RecommendList(cards.map { it.name }))
            highlight.cancel()
            onReading(null)
        }
        listenForChoice(cards, onSelect, onNext, onCancel)
    }

    /**
     * 추천 단계에서 듣는다 (기획서 6장 '지금 단계에서 받는 의도'). 없는 번호·같은 이름·그 밖의 말은 각각 한 번만 되묻고,
     * 그다음부터는 무시하고 계속 듣는다. 말이 없으면 끝(카드는 그대로, 터치로 고를 수 있음).
     */
    private suspend fun listenForChoice(cards: List<RecCard>, onSelect: (Int) -> Unit, onNext: () -> Unit, onCancel: () -> Unit) {
        val asked = mutableSetOf<VoicePrompt>()
        suspend fun once(prompt: VoicePrompt) {
            if (asked.add(prompt)) tts.speak(prompt)
        }
        repeat(MAX_TURNS) {
            val out = listen.run(ListenRequest(noSpeechTimeoutMs = NO_SPEECH_MS))
            if (out !is ListenOutcome.Heard) return
            val p = parser.parse(out.text, VoiceStage.RECOMMEND, cards.map { it.name })
            when {
                p.intent == Intent.CANCEL -> return onCancel()
                p.intent == Intent.NEXT -> return onNext()
                p.intent == Intent.SELECT && p.ambiguous -> once(VoicePrompt.WhichNumber)
                p.intent == Intent.SELECT && p.selectedIndex in cards.indices -> return onSelect(p.selectedIndex!!)
                p.intent == Intent.SELECT -> once(VoicePrompt.ChooseRange(cards.size))
                else -> once(VoicePrompt.SayNumber)
            }
        }
    }

    private companion object {
        const val PER_ITEM_MS = 1_600L
        const val NO_SPEECH_MS = 8_000L
        /** 알아듣지 못한 말이 이어져도 듣기를 끝없이 되풀이하지 않게 (화면은 2분 동안 그대로). */
        const val MAX_TURNS = 6
    }
}
