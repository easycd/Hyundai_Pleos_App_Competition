package com.easycd.nextplace.feature.visit.note

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.VisitNoteRunner
import com.easycd.nextplace.core.domain.model.Visit
import com.easycd.nextplace.core.domain.model.VisitState
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.repository.VisitRepository
import com.easycd.nextplace.core.domain.repository.VisitSessionRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import javax.inject.Inject

/**
 * 홈 음성 "여기 괜찮았어" (S-15b, 기술서 7-4 4): 최근 3시간 안에 출발했거나 머무는 중인 가장 최근 방문에 메모를 단다.
 * '머무는 중'은 방문 판정 세션이 방문을 인정한 상태(VISITED)일 때만 본다. 그런 방문이 없으면 null → 저장으로 처리한다.
 */
class AttachNoteToRecentVisit @Inject constructor(
    private val visits: VisitRepository,
    private val sessions: VisitSessionRepository,
    private val places: PlaceRepository,
    private val clock: Clock,
) : VisitNoteRunner {
    override suspend fun attachNoteToRecentVisit(memo: String): String? {
        val v = pick(staying(), visits.latestSince(clock.now() - Tunables.NOTE_RECENT_WINDOW_MS)) ?: return null
        val place = places.get(v.placeId) ?: return null
        visits.setNote(v.id, memo)
        return place.name ?: place.displayName
    }

    private suspend fun staying(): Visit? {
        val s = sessions.get()?.takeIf { it.state == VisitState.VISITED } ?: return null
        return s.visitId?.let { visits.get(it) }
    }

    private fun pick(a: Visit?, b: Visit?): Visit? = listOfNotNull(a, b).maxByOrNull { it.arrivedAt }
}
