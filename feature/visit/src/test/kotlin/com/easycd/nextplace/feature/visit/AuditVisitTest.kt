package com.easycd.nextplace.feature.visit

import com.easycd.nextplace.core.domain.model.VisitSession
import com.easycd.nextplace.core.domain.model.VisitState
import com.easycd.nextplace.feature.visit.tracker.VisitStep
import com.easycd.nextplace.feature.visit.tracker.VisitStepResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 전수점검 보고서(docs/05) 재현 테스트: B-04 · B-07. */
class AuditVisitTest {
    private val minute = 60_000L

    @Test fun restartAfterLongLocationGapDoesNotProveTenMinuteStay() {
        val persisted = VisitSession("p", VisitState.STAYING, 0, 4 * minute,
            arrivedAt = 0, stayStartedAt = 0, lastInsideAt = 4 * minute)
        // 앱을 다시 켜면 VisitTracker.lastSampleAt 은 null 로 시작한다
        val result = VisitStep.next(persisted, 20.0, 20 * minute, null)
        assertTrue("A 16-minute location gap must restart the dwell timer: $result", result is VisitStepResult.Keep)
        assertEquals(20 * minute, (result as VisitStepResult.Keep).session.stayStartedAt)
    }

    @Test fun outsideForFortyFiveSecondsResetsEvenWithoutIntermediateSamples() {
        val staying = VisitSession("p", VisitState.STAYING, 0, 9 * minute,
            arrivedAt = 0, stayStartedAt = 0, lastInsideAt = 9 * minute)
        val outsideAt = 9 * minute + 20_000
        val out = VisitStep.next(staying, 200.0, outsideAt, 9 * minute) as VisitStepResult.Keep
        val backAt = outsideAt + 45_000
        val result = VisitStep.next(out.session, 20.0, backAt, outsideAt)
        assertTrue("45 seconds outside must restart the dwell timer: $result", result is VisitStepResult.Keep)
        assertEquals(backAt, (result as VisitStepResult.Keep).session.stayStartedAt)
    }
}
