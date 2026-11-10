package com.easycd.nextplace.feature.myplaces.usecase

import com.easycd.nextplace.core.domain.model.LocationQuality
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.usecase.withDbRetry
import com.easycd.nextplace.core.domain.voice.Categories
import javax.inject.Inject

sealed interface ResolveResult {
    data class Saved(val id: String) : ResolveResult
    data class Merged(val id: String) : ResolveResult
    data object Failed : ResolveResult
}

/**
 * 미확인 정리 · 후보 다시 고르기 (S-09, S-09b, S-08b): 고른 후보로 확정. 이름·주소·좌표만 바뀌고
 * 내 메모·저장일·알림 설정은 그대로. 이미 저장한 가게를 고르면 두 기록을 하나로 합친다.
 */
class ResolvePlaceUseCase @Inject constructor(private val places: PlaceRepository) {
    suspend operator fun invoke(place: Place, c: PoiCandidate): ResolveResult = withDbRetry {
        val existing = places.findByProvider(c.provider, c.providerId)
        if (existing != null && existing.id != place.id) {
            if (existing.deletedAt != null) places.restore(existing.id)
            places.mergeInto(existing.id, place)
            ResolveResult.Merged(existing.id)
        } else {
            val category = Categories.label(c.category, place.cues.category)
            places.update(
                place.copy(
                    name = c.name,
                    category = category,
                    address = c.address,
                    lat = c.lat,
                    lon = c.lon,
                    locationQuality = LocationQuality.POI,
                    provider = c.provider,
                    providerId = c.providerId,
                    status = PlaceStatus.CONFIRMED,
                    candidatesFoundLater = false,
                ),
            )
            ResolveResult.Saved(place.id)
        }
    }.getOrDefault(ResolveResult.Failed)
}
