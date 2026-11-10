package com.easycd.nextplace.feature.voicesave.recovery

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.usecase.withDbRetry
import javax.inject.Inject

/**
 * 앱 시작 때 1번 (기술서 7-1·8장): 지난 실행에서 끝나지 못한 초안을 미확인으로 마무리하고,
 * 4초 되돌리기 중에 꺼진 삭제를 확정하고, 후보 없는 미확인 가게가 있으면 재검색을 예약한다.
 */
class SaveRecovery @Inject constructor(
    private val places: PlaceRepository,
    private val clock: Clock,
    private val research: ResearchScheduler,
) {
    suspend fun run() {
        withDbRetry { places.purgeMarkedDeleted() }
        val stale = places.staleDrafts(clock.now() - Tunables.DRAFT_STALE_MS)
        stale.forEach { d -> withDbRetry { places.update(d.copy(draft = false, status = PlaceStatus.UNCONFIRMED)) } }
        if (places.unconfirmedWithoutCandidates().any { it.point != null }) research.schedule()
    }
}
