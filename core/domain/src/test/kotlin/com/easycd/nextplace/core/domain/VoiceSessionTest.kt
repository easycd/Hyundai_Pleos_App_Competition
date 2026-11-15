package com.easycd.nextplace.core.domain

import com.easycd.nextplace.core.domain.voice.VoiceSession
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** 기술서 7-0 우선순위: 사용자 > 저장 확인 > 방문 메모 > 리마인드. 리마인드는 30초 넘게 기다리면 버린다. */
@OptIn(ExperimentalCoroutinesApi::class)
class VoiceSessionTest {
    @Test fun visitNoteGoesBeforeRemindEvenIfRemindQueuedFirst() = runTest {
        val voice = VoiceSession()
        val order = mutableListOf<String>()
        val release = CompletableDeferred<Unit>()
        launch { voice.runUser { release.await() } }
        runCurrent()
        launch { voice.runQueued(VoiceSession.Kind.REMIND, 30_000) { order += "remind" } }
        runCurrent()
        launch { voice.runQueued(VoiceSession.Kind.VISIT_NOTE, 600_000) { order += "note" } }
        runCurrent()
        release.complete(Unit)
        runCurrent()
        assertEquals(listOf("note", "remind"), order)
    }

    @Test fun remindWaitingTooLongIsDropped() = runTest {
        val voice = VoiceSession()
        val release = CompletableDeferred<Unit>()
        launch { voice.runUser { release.await() } }
        advanceUntilIdle()
        var result: Unit? = Unit
        val waiter = launch { result = voice.runQueued(VoiceSession.Kind.REMIND, 30_000) { } }
        testScheduler.advanceTimeBy(30_001)
        advanceUntilIdle()
        waiter.join()
        assertNull(result)
        release.complete(Unit)
    }
}
