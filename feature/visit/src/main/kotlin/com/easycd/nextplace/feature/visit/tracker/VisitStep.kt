package com.easycd.nextplace.feature.visit.tracker

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.model.VisitSession
import com.easycd.nextplace.core.domain.model.VisitState

/** 위치 한 번에 대한 방문 판정 결과. */
sealed interface VisitStepResult {
    /** 세션을 버림 (안내 뒤 3시간 안에 도착·체류하지 않음). */
    data object Drop : VisitStepResult
    data class Keep(val session: VisitSession) : VisitStepResult
    /** 누적 10분이 찼음: Visit 을 만들고 visitId 를 채워 저장. */
    data class Record(val session: VisitSession) : VisitStepResult
    /** 100m 밖 1분: 출발. leftAt 기록 + S-15. */
    data class Left(val session: VisitSession) : VisitStepResult
}

/**
 * 방문 판정 순수 상태 전이 (기술서 7-4). 저장소를 모르므로 시각과 거리만으로 시험할 수 있다.
 * ROUTING → ARRIVED(내비 도착 또는 50m) → STAYING(100m 안) → VISITED(누적 10분) → LEFT(100m 밖 1분).
 * 위치 공백 2분까지는 머문 것으로 보고, 100m 밖이 30초 넘게 이어지면 타이머를 처음부터 다시 잰다.
 * 직전 위치 시각은 [prevSampleAt](이번 실행에서 받은 것), 없으면(앱을 다시 켬) 저장된 세션의 updatedAt 을 쓴다.
 * 그래야 앱이 꺼져 있던 긴 공백이 체류로 잡히지 않는다.
 */
object VisitStep {
    fun next(s: VisitSession, distanceM: Double, now: Long, prevSampleAt: Long?): VisitStepResult {
        val inside = distanceM <= Tunables.VISIT_STAY_RADIUS_M
        val gapOk = now - (prevSampleAt ?: s.updatedAt) <= Tunables.VISIT_GAP_TOLERANCE_MS
        return when (s.state) {
            VisitState.ROUTING -> when {
                now - s.startedAt > Tunables.VISIT_ROUTING_EXPIRE_MS -> VisitStepResult.Drop
                distanceM <= Tunables.VISIT_ARRIVE_RADIUS_M ->
                    stay(s.copy(state = VisitState.ARRIVED, arrivedAt = now, updatedAt = now), true, now, true)
                else -> VisitStepResult.Keep(s)
            }
            VisitState.ARRIVED, VisitState.STAYING -> {
                // 도착했지만 3시간 넘게 10분을 채우지 못하면 버린다 (위치를 계속 받지 않도록).
                if (now - (s.arrivedAt ?: s.startedAt) > Tunables.VISIT_ROUTING_EXPIRE_MS) VisitStepResult.Drop else stay(s, inside, now, gapOk)
            }
            VisitState.VISITED -> {
                val out = s.outsideSince
                when {
                    inside -> VisitStepResult.Keep(s.copy(outsideSince = null, lastInsideAt = now, updatedAt = now))
                    // 위치 공백 뒤 첫 '밖' 표본: 그동안 어디 있었는지 모르므로 1분을 지금부터 다시 잰다
                    out == null || !gapOk -> VisitStepResult.Keep(s.copy(outsideSince = now, updatedAt = now))
                    now - out >= Tunables.VISIT_LEFT_MS -> VisitStepResult.Left(s.copy(state = VisitState.LEFT, updatedAt = now))
                    else -> VisitStepResult.Keep(s)
                }
            }
            VisitState.LEFT -> VisitStepResult.Drop
        }
    }

    private fun stay(s: VisitSession, inside: Boolean, now: Long, gapOk: Boolean): VisitStepResult {
        if (!inside) {
            val since = s.outsideSince ?: now
            val reset = now - since > Tunables.VISIT_OUT_RESET_MS
            return VisitStepResult.Keep(
                if (reset) s.copy(state = VisitState.ARRIVED, stayStartedAt = null, outsideSince = since, updatedAt = now) else s.copy(outsideSince = since, updatedAt = now),
            )
        }
        // 밖에 30초 넘게 있다가 돌아왔으면(사이 표본이 없어도) 처음부터 다시 잰다
        val wasOutTooLong = s.outsideSince?.let { now - it > Tunables.VISIT_OUT_RESET_MS } ?: false
        val start = s.stayStartedAt?.takeIf { gapOk && !wasOutTooLong } ?: now
        val staying = s.copy(state = VisitState.STAYING, stayStartedAt = start, lastInsideAt = now, outsideSince = null, updatedAt = now)
        return if (now - start < Tunables.VISIT_STAY_MS) VisitStepResult.Keep(staying) else VisitStepResult.Record(staying.copy(state = VisitState.VISITED))
    }
}
