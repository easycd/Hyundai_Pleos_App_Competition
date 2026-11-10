package com.easycd.nextplace.feature.voicesave.recovery

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
        // 앱(프로세스)이 막 시작했으니 지금 있는 초안은 모두 지난 실행의 것이다. 나이와 상관없이 마무리한다
        // (1분보다 새 초안만 남기면, 1분 안에 다시 켰을 때 그 초안이 다음 실행까지 목록에서 숨는다).
        val stale = places.staleDrafts(clock.now())
        stale.forEach { d -> withDbRetry { places.update(d.copy(draft = false, status = PlaceStatus.UNCONFIRMED)) } }
        if (places.unconfirmedWithoutCandidates().any { it.point != null }) research.schedule()
    }
}
