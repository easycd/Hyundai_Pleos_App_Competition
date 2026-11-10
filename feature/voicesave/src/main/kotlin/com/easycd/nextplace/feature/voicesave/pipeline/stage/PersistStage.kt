package com.easycd.nextplace.feature.voicesave.pipeline.stage

import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.LocationQuality
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.pipeline.Stage
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.usecase.withDbRetry
import com.easycd.nextplace.feature.voicesave.recovery.ResearchScheduler
import javax.inject.Inject

sealed interface PersistKind {
    /** 확정: 고른 후보로 저장. */
    data class Confirmed(val candidate: PoiCandidate, val category: String?, val candidates: List<PoiCandidate>) : PersistKind

    /** 미확인: 추정 지점(없으면 위치 없음)과 후보만. */
    data class Unconfirmed(val point: GeoPoint?, val candidates: List<PoiCandidate>, val roadName: String? = null) : PersistKind
}

data class PersistInput(val draft: Place, val kind: PersistKind)

sealed interface PersistResult {
    data class Saved(val name: String, val id: String) : PersistResult
    data class Merged(val name: String, val id: String) : PersistResult
    data object LocationOnly : PersistResult
    data object TextOnly : PersistResult
    data object Failed : PersistResult
}

/**
 * 8단계 저장 (S-04a · S-04b): 초안 행을 확정 또는 미확인으로 갱신(draft = false).
 * 같은 가게(provider·providerId)가 이미 있으면 초안을 지우고 기존 행에 단서만 덧붙인다 (기술서 8장 합치기).
 * DB 오류는 3회 재시도, 끝내 실패면 Failed (저장했다고 말하지 않음).
 */
class PersistStage @Inject constructor(
    private val places: PlaceRepository,
    private val research: ResearchScheduler,
) : Stage<PersistInput, PersistResult> {
    override val name = "8 Persist"

    override suspend fun run(input: PersistInput): PersistResult {
        val result = withDbRetry {
            when (val k = input.kind) {
                is PersistKind.Confirmed -> confirmed(input.draft, k)
                is PersistKind.Unconfirmed -> unconfirmed(input.draft, k)
            }
        }
        return result.getOrDefault(PersistResult.Failed)
    }

    private suspend fun confirmed(draft: Place, k: PersistKind.Confirmed): PersistResult {
        val c = k.candidate
        val existing = places.findByProvider(c.provider, c.providerId)
        if (existing != null && existing.id != draft.id) {
            if (existing.deletedAt != null) places.restore(existing.id)
            places.mergeInto(existing.id, draft)
            return PersistResult.Merged(existing.name ?: c.name, existing.id)
        }
        places.update(
            draft.copy(
                name = c.name,
                category = k.category,
                address = c.address,
                lat = c.lat,
                lon = c.lon,
                locationQuality = LocationQuality.POI,
                provider = c.provider,
                providerId = c.providerId,
                status = PlaceStatus.CONFIRMED,
                draft = false,
                candidates = k.candidates,
            ),
        )
        return PersistResult.Saved(c.name, draft.id)
    }

    private suspend fun unconfirmed(draft: Place, k: PersistKind.Unconfirmed): PersistResult {
        places.update(
            draft.copy(
                address = draft.address ?: k.roadName,
                lat = k.point?.lat,
                lon = k.point?.lon,
                locationQuality = if (k.point != null) LocationQuality.ESTIMATED else LocationQuality.NONE,
                status = PlaceStatus.UNCONFIRMED,
                draft = false,
                candidates = k.candidates,
            ),
        )
        // 오프라인·시간 초과로 후보 없이 저장했으면 네트워크 복구 뒤 재검색 (홈 '후보 찾음' 칩)
        if (k.point != null && k.candidates.isEmpty()) research.schedule()
        return if (k.point != null) PersistResult.LocationOnly else PersistResult.TextOnly
    }
}
