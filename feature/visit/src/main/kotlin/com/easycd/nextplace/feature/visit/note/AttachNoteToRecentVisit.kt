package com.easycd.nextplace.feature.visit.note

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.VisitNoteRunner
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.repository.VisitRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import javax.inject.Inject

/**
 * 홈 음성 "여기 괜찮았어" (S-15b): 최근 3시간 안에 출발했거나 머무는 중인 가장 최근 방문에 메모를 단다.
 * 그런 방문이 없으면 null → 저장으로 처리한다.
 */
class AttachNoteToRecentVisit @Inject constructor(
    private val visits: VisitRepository,
    private val places: PlaceRepository,
    private val clock: Clock,
) : VisitNoteRunner {
    override suspend fun attachNoteToRecentVisit(memo: String): String? {
        val v = visits.latestSince(clock.now() - Tunables.NOTE_RECENT_WINDOW_MS) ?: return null
        val place = places.get(v.placeId) ?: return null
        visits.setNote(v.id, memo)
        return place.name ?: place.displayName
    }
}
