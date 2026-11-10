package com.easycd.nextplace.feature.visit

import com.easycd.nextplace.core.domain.model.VisitSession
import com.easycd.nextplace.core.domain.model.VisitState
import com.easycd.nextplace.feature.visit.tracker.VisitStep
import com.easycd.nextplace.feature.visit.tracker.VisitStepResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 기술서 10장: 방문 9분 이탈 · 위치 공백 · 10분 체류 · 출발 1분 경계. */
class VisitStepTest {
    private val sec = 1_000L
    private val min = 60 * sec

    /** [samples] = (시각, 거리) 를 차례로 넣고 마지막 결과와 기록 여부를 돌려준다. */
    private fun run(start: VisitSession, samples: List<Pair<Long, Double>>): Pair<VisitSession?, List<String>> {
        var s: VisitSession? = start
        var prev: Long? = null
        val events = mutableListOf<String>()
        for ((t, d) in samples) {
            val cur = s ?: break
            when (val r = VisitStep.next(cur, d, t, prev)) {
                VisitStepResult.Drop -> { s = null; events += "drop" }
                is VisitStepResult.Keep -> s = r.session
                is VisitStepResult.Record -> { s = r.session.copy(visitId = "v"); events += "record@${t / sec}" }
                is VisitStepResult.Left -> { s = null; events += "left@${t / sec}" }
            }
            prev = t
        }
        return s to events
    }

    private fun routing(at: Long = 0) = VisitSession("p", VisitState.ROUTING, at, at)
    private fun everySec(from: Long, to: Long, d: Double) = (from until to step 10 * sec).map { it to d }

    @Test fun tenMinutesInsideRecordsVisit() {
        val (s, ev) = run(routing(), everySec(0, 11 * min, 30.0))
        assertEquals(listOf("record@600"), ev)
        assertEquals(VisitState.VISITED, s?.state)
    }

    @Test fun nineMinutesThenLeaveResetsTimer() {
        // 9분 머문 뒤 40초 밖(밖 샘플 9:00~9:40) → 타이머 처음부터. 다시 9분 머물러도 기록 없음.
        val samples = everySec(0, 9 * min, 30.0) + everySec(9 * min, 9 * min + 50 * sec, 300.0) + everySec(9 * min + 50 * sec, 18 * min + 50 * sec, 30.0)
        val (s, ev) = run(routing(), samples)
        assertTrue(ev.isEmpty())
        assertEquals(VisitState.STAYING, s?.state)
    }

    @Test fun shortStepOutKeepsTimer() {
        // 20초만 밖 → 이어서 잼, 10분째 기록.
        val samples = everySec(0, 5 * min, 30.0) + everySec(5 * min, 5 * min + 20 * sec, 300.0) + everySec(5 * min + 20 * sec, 11 * min, 30.0)
        assertEquals(listOf("record@600"), run(routing(), samples).second)
    }

    @Test fun locationGapUpToTwoMinutesCountsAsStaying() {
        val samples = everySec(0, 4 * min + 10 * sec, 30.0) + everySec(6 * min, 11 * min, 30.0) // 4분→6분 사이 2분 공백
        assertEquals(listOf("record@600"), run(routing(), samples).second)
    }

    @Test fun longerGapRestartsTimer() {
        val samples = everySec(0, 4 * min, 30.0) + everySec(7 * min, 12 * min, 30.0) // 3분 공백
        assertTrue(run(routing(), samples).second.isEmpty())
    }

    @Test fun leavingAfterVisitAsksNote() {
        val samples = everySec(0, 11 * min, 30.0) + everySec(11 * min, 13 * min, 300.0)
        assertEquals(listOf("record@600", "left@720"), run(routing(), samples).second)
    }

    @Test fun routingExpiresAfterThreeHours() {
        val (s, ev) = run(routing(), listOf(0L to 5_000.0, 3 * 60 * min + sec to 5_000.0))
        assertEquals(null, s)
        assertEquals(listOf("drop"), ev)
    }

    @Test fun arrivedButNeverStayedExpires() {
        val arrived = VisitSession("p", VisitState.ARRIVED, 0, 0, arrivedAt = 0)
        assertEquals(VisitStepResult.Drop, VisitStep.next(arrived, 500.0, 3 * 60 * min + sec, null))
    }
}
