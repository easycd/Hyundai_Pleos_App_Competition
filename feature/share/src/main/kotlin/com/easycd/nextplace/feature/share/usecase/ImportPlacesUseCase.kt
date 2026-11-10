package com.easycd.nextplace.feature.share.usecase

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.LocationQuality
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.model.SharedPlace
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import java.util.UUID
import javax.inject.Inject

/**
 * 받은 목록 가져오기 (S-16): 이미 있는 가게(같은 카카오 ID, 또는 50m 안 같은 이름)는 고를 수 없다.
 * 선택한 가게를 한 번에 저장하고, 하나라도 실패하면 아무것도 저장하지 않는다.
 * 받은 가게는 확인된 가게 · 근처 알림 켬 · 방문 기록 없음 · 보낸 메모는 내 메모로.
 */
class ImportPlacesUseCase @Inject constructor(private val places: PlaceRepository, private val clock: Clock) {

    suspend fun exists(p: SharedPlace): Boolean {
        val provider = p.provider
        val pid = p.providerId
        val byId = if (provider != null && pid != null) places.findByProvider(provider, pid) else null
        if (byId != null && byId.deletedAt == null) return true
        return places.findSimilar(p.name, GeoPoint(p.lat, p.lon), Tunables.SAME_PLACE_DISTANCE_M) != null
    }

    suspend fun import(list: List<SharedPlace>): Result<List<String>> = runCatching {
        val now = clock.now()
        // 같은 카카오 ID 가 이미 행으로 있으면(방금 지워 '삭제 예정'인 것 포함) 새로 넣지 않는다.
        // UNIQUE(provider, providerId) 때문에 넣으면 전체가 실패한다. 삭제 예정인 것은 되살린다.
        val known = list.associateWith { s ->
            val provider = s.provider
            val pid = s.providerId
            if (provider != null && pid != null) places.findByProvider(provider, pid) else null
        }
        val fresh = list.filter { known[it] == null }
        val rows = fresh.mapIndexed { i, s ->
            Place(
                id = UUID.randomUUID().toString(),
                name = s.name,
                category = s.category,
                address = s.address,
                lat = s.lat,
                lon = s.lon,
                locationQuality = LocationQuality.POI,
                provider = s.provider,
                providerId = s.providerId,
                status = PlaceStatus.CONFIRMED,
                source = PlaceSource.SHARED,
                draft = false,
                utterance = null,
                cues = Cues(),
                memo = s.memo,
                savedAt = now + i,
                savedLat = null,
                savedLon = null,
                savedHeading = null,
                candidates = emptyList(),
                remindEnabled = true,
                lastRemindedAt = null,
                deletedAt = null,
            )
        }
        places.insertAll(rows)
        val revived = known.values.filterNotNull().onEach { if (it.deletedAt != null) places.restore(it.id) }
        rows.map { it.id } + revived.map { it.id }
    }
}
