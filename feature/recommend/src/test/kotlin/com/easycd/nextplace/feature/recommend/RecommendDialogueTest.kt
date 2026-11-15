package com.easycd.nextplace.feature.recommend

import com.easycd.nextplace.core.domain.pipeline.ListenStage
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import com.easycd.nextplace.core.domain.sdk.SpeechRecognizer
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.sdk.SttEvent
import com.easycd.nextplace.core.domain.voice.IntentParser
import com.easycd.nextplace.core.domain.voice.SlotExtractor
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.feature.recommend.session.RecCard
import com.easycd.nextplace.feature.recommend.session.RecommendDialogue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** 기획서 6장 '추천 선택(S-06)': 그 밖의 말은 한 번 "번호로 말해 주세요", 그다음은 무시하고 계속 듣는다. */
@OptIn(ExperimentalCoroutinesApi::class)
class RecommendDialogueTest {
    private val cards = listOf(RecCard("a", 1, "서울갈비", null, 100.0, null), RecCard("b", 2, "명품갈비", null, 200.0, null), RecCard("c", 3, "카페", null, 300.0, null))

    private class Script(lines: List<String>) : SpeechRecognizer {
        private val queue = ArrayDeque(lines)
        override val status = MutableStateFlow(SdkStatus.READY)
        override fun listen(): Flow<SttEvent> {
            val line = queue.removeFirstOrNull() ?: return emptyFlow()
            return flowOf(SttEvent.Started, SttEvent.Partial(line), SttEvent.Final(line))
        }
    }

    private class Recorder : SpeechSynthesizer {
        val said = mutableListOf<VoicePrompt>()
        override val status = MutableStateFlow(SdkStatus.READY)
        override val speaking = MutableStateFlow(false)
        override suspend fun speak(prompt: VoicePrompt): Boolean = said.add(prompt)
        override fun stop() {}
        override fun setVolume(volume: Float) {}
    }

    private fun TestScope.clock() = object : Clock {
        override fun now() = testScheduler.currentTime
        override fun hourOfDay(atMs: Long) = 12
        override fun dayIndex(atMs: Long) = 0L
    }

    private fun TestScope.dialogue(tts: Recorder, lines: List<String>) =
        RecommendDialogue(tts, ListenStage(Script(lines), clock()), IntentParser(SlotExtractor()))

    @Test fun unknownWordsAreAnsweredOnceThenStillListening() = runTest {
        val tts = Recorder()
        var selected: Int? = null
        dialogue(tts, listOf("음 글쎄", "아무거나", "2번")).run(cards, {}, { selected = it }, {}, {}, readList = false)
        assertEquals(1, selected)
        assertEquals(listOf<VoicePrompt>(VoicePrompt.SayNumber), tts.said)
    }

    @Test fun outOfRangeNumberIsAnsweredOnceThenAValidNumberStillWorks() = runTest {
        val tts = Recorder()
        var selected: Int? = null
        dialogue(tts, listOf("5번", "7번", "1번")).run(cards, {}, { selected = it }, {}, {}, readList = false)
        assertEquals(0, selected)
        assertEquals(listOf<VoicePrompt>(VoicePrompt.ChooseRange(3)), tts.said)
    }

    @Test fun silenceEndsListeningWithoutChoosing() = runTest {
        val tts = Recorder()
        var selected: Int? = null
        dialogue(tts, emptyList()).run(cards, {}, { selected = it }, {}, {}, readList = false)
        assertEquals(null, selected)
        assertEquals(emptyList<VoicePrompt>(), tts.said)
    }
}
