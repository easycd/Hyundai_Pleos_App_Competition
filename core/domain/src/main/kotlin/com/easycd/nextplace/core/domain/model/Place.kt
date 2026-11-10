package com.easycd.nextplace.core.domain.model

/** 저장 가게 (기획서 7장 Place). */
data class Place(
    val id: String,
    val name: String?,
    val category: String?,
    val address: String?,
    val lat: Double?,
    val lon: Double?,
    val locationQuality: LocationQuality,
    val provider: String?,
    val providerId: String?,
    val status: PlaceStatus,
    val source: PlaceSource,
    val draft: Boolean,
    val utterance: String?,
    val cues: Cues,
    val memo: String?,
    val savedAt: Long,
    val savedLat: Double?,
    val savedLon: Double?,
    val savedHeading: Double?,
    val candidates: List<PoiCandidate>,
    val remindEnabled: Boolean,
    val lastRemindedAt: Long?,
    val deletedAt: Long?,
    /** 오프라인 저장 뒤 재검색으로 후보를 찾음 → 홈 칩 '후보 찾음' (기획서 6장 실패 처리). */
    val candidatesFoundLater: Boolean = false,
) {
    val point: GeoPoint? get() = if (lat != null && lon != null) GeoPoint(lat, lon) else null

    /** 미확인이면 1순위 후보 이름에 '?'를 붙인다 (기획서 7장 name). 후보도 없으면 null. */
    val displayName: String?
        get() = when {
            status == PlaceStatus.CONFIRMED -> name
            candidates.isNotEmpty() -> candidates.first().name + "?"
            name != null -> "$name?"
            else -> null
        }
}

/** 목록·추천에서 함께 쓰는 가게 + 방문 요약. */
data class PlaceWithVisits(
    val place: Place,
    val visitCount: Int,
    val lastVisitAt: Long?,
    val visitNotes: List<String>,
)
