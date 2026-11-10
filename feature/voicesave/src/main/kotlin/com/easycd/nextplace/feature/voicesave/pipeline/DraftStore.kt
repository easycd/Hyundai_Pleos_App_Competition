package com.easycd.nextplace.feature.voicesave.pipeline

import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.LocationQuality
import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.usecase.withDbRetry
import java.util.UUID
import javax.inject.Inject

/**
 * 2단계 끝의 초안 행 (기술서 7-1): 원문과 스냅샷을 먼저 쓴다. 그 뒤 앱이 꺼져도 기록은 남고
 * 다음 실행 때 미확인으로 마무리된다. 쓰기 실패면 null (8단계에서 "지금은 저장하지 못했어요").
 */
class DraftStore @Inject constructor(private val places: PlaceRepository) {

    suspend fun create(snapshot: LocationSnapshot, utterance: String?, source: PlaceSource = snapshot.source): Place? {
        val cur = snapshot.current
        val draft = Place(
            id = UUID.randomUUID().toString(),
            name = null,
            category = null,
            address = null,
            lat = cur?.lat,
            lon = cur?.lon,
            locationQuality = if (cur != null) LocationQuality.ESTIMATED else LocationQuality.NONE,
            provider = null,
            providerId = null,
            status = PlaceStatus.UNCONFIRMED,
            source = source,
            draft = true,
            utterance = utterance,
            cues = Cues(),
            memo = null,
            savedAt = snapshot.pressedAtMs,
            savedLat = cur?.lat,
            savedLon = cur?.lon,
            savedHeading = cur?.heading?.toDouble(),
            candidates = emptyList(),
            remindEnabled = true,
            lastRemindedAt = null,
            deletedAt = null,
        )
        return withDbRetry { places.insert(draft) }.map { draft }.getOrNull()
    }

    suspend fun delete(id: String) {
        withDbRetry { places.deleteNow(id) }
    }
}
