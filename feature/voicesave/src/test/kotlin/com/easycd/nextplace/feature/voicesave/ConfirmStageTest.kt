package com.easycd.nextplace.feature.voicesave

import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.pipeline.ListenStage
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import com.easycd.nextplace.core.domain.sdk.SpeechRecognizer
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.sdk.SttEvent
import com.easycd.nextplace.core.domain.voice.IntentParser
import com.easycd.nextplace.core.domain.voice.SlotExtractor
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.feature.voicesave.pipeline.ConfirmAnswer
import com.easycd.nextplace.feature.voicesave.pipeline.SaveFlowState
import com.easycd.nextplace.feature.voicesave.pipeline.stage.ConfirmIO
import com.easycd.nextplace.feature.voicesave.pipeline.stage.ConfirmInput
import com.easycd.nextplace.feature.voicesave.pipeline.stage.ConfirmResult
import com.easycd.nextplace.feature.voicesave.pipeline.stage.ConfirmStage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** 화면 설계서 S-03: 알아듣지 못한 대답은 무응답과 같다 (대답으로 치지 않고, TTS 가 끝난 뒤 5초까지 계속 듣는다). */
@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmStageTest {
    private val candidate = PoiCandidate("kakao", "1", "서울갈비", "음식점 > 한식 > 육류,고기 > 갈비", "서울", 37.5, 127.0)

    /** 줄마다 [gapMs] 쉬었다가 말한다. 줄이 없으면 아무 말도 하지 않는다 (마이크는 열려 있음). */
    private class Script(lines: List<String>, private val gapMs: Long = 0) : SpeechRecognizer {
        private val queue = ArrayDeque(lines)
        override val status = MutableStateFlow(SdkStatus.READY)
        override fun listen(): Flow<SttEvent> = flow {
            emit(SttEvent.Started)
            val line = queue.removeFirstOrNull()
            if (line == null) {
                delay(Long.MAX_VALUE)
            } else {
                delay(gapMs)
                emit(SttEvent.Partial(line))
                emit(SttEvent.Final(line))
            }
        }
    }

    private object Silent : SpeechSynthesizer {
        override val status = MutableStateFlow(SdkStatus.READY)
        override val speaking = MutableStateFlow(false)
        override suspend fun speak(prompt: VoicePrompt) = true
        override fun stop() {}
        override fun setVolume(volume: Float) {}
    }

    private class Io : ConfirmIO {
        override fun show(state: SaveFlowState) {}
        override val answers = Channel<ConfirmAnswer>(Channel.CONFLATED)
    }

    private fun TestScope.stage(script: Script): ConfirmStage {
        val clock = object : Clock {
            override fun now() = testScheduler.currentTime
            override fun hourOfDay(atMs: Long) = 12
            override fun dayIndex(atMs: Long) = 0L
        }
        return ConfirmStage(Silent, ListenStage(script, clock), IntentParser(SlotExtractor()), clock)
    }

    private fun input() = ConfirmInput(listOf(candidate), Cues(), LocationSnapshot(1, 0, null, emptyList()), Io())

    @Test fun unclearWordThenYesWithinFiveSecondsSaves() = runTest {
        val r = stage(Script(listOf("음 잠깐만", "응"), gapMs = 500)).run(input())
        assertEquals(ConfirmResult.Chosen(candidate), r)
    }

    @Test fun unclearWordOnlyEndsAsUnconfirmed() = runTest {
        val r = stage(Script(listOf("음 잠깐만"), gapMs = 500)).run(input())
        assertEquals(ConfirmResult.Unconfirmed, r)
    }

    @Test fun cancelWordCancels() = runTest {
        val r = stage(Script(listOf("취소"))).run(input())
        assertEquals(ConfirmResult.Cancelled, r)
    }
}
