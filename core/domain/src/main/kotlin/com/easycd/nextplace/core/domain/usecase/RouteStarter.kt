package com.easycd.nextplace.core.domain.usecase

import com.easycd.nextplace.core.domain.contract.RouteNotice
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
 * 보내기 직전에 확인 문구를 차량 알림으로 띄우고(S-05b), 실패면 1회 재시도. 성공하면 VisitSession(ROUTING)을 만든다 (기술서 7-3 5, 7-4 1).
 */
class RouteStarter @Inject constructor(
    private val navi: NaviController,
    private val sessions: VisitSessionRepository,
    private val clock: Clock,
    private val notice: RouteNotice,
) {
    /** 이미 목적지가 있으면 "목적지를 ○○로 바꿀게요". */
    suspend fun hasDestination(): Boolean = navi.currentDestination() != null

    fun targetOf(place: Place): RouteTarget? {
        val p = place.point ?: return null
        return RouteTarget(p, place.name ?: place.displayName ?: "", place.address)
    }

    /** [changing] = 이미 목적지가 있었음 (확인 문구가 "목적지를 ○○로 바꿀게요"가 된다). */
    suspend fun send(place: Place, changing: Boolean = false): Boolean {
        val target = targetOf(place) ?: return false
        notice.show(target.name, changing)
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
