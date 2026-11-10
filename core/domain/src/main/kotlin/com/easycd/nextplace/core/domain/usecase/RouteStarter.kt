package com.easycd.nextplace.core.domain.usecase

import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.VisitSession
import com.easycd.nextplace.core.domain.model.VisitState
import com.easycd.nextplace.core.domain.repository.VisitSessionRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.NaviController
import com.easycd.nextplace.core.domain.sdk.RouteTarget
import javax.inject.Inject

/**
 * Pleos 내비에 목적지만 넘긴다 (F-10). 추천(S-06c)·리마인드(S-05b)·가게 상세(S-08)가 같이 쓴다.
 * 실패면 1회 재시도. 성공하면 VisitSession(ROUTING)을 만든다 (기술서 7-3 5, 7-4 1).
 */
class RouteStarter @Inject constructor(
    private val navi: NaviController,
    private val sessions: VisitSessionRepository,
    private val clock: Clock,
) {
    /** 이미 목적지가 있으면 "목적지를 ○○로 바꿀게요". */
    suspend fun hasDestination(): Boolean = navi.currentDestination() != null

    fun targetOf(place: Place): RouteTarget? {
        val p = place.point ?: return null
        return RouteTarget(p, place.name ?: place.displayName ?: "", place.address)
    }

    suspend fun send(place: Place): Boolean {
        val target = targetOf(place) ?: return false
        repeat(ATTEMPTS) {
            if (navi.requestRoute(target)) {
                val now = clock.now()
                sessions.save(VisitSession(place.id, VisitState.ROUTING, now, now))
                return true
            }
        }
        return false
    }

    private companion object {
        const val ATTEMPTS = 2
    }
}
