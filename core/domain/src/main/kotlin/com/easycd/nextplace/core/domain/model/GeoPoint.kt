package com.easycd.nextplace.core.domain.model

data class GeoPoint(val lat: Double, val lon: Double)

/** 위치 표본 1개. 시각은 SDK 시각(ms). */
data class LocationSample(
    val timeMs: Long,
    val lat: Double,
    val lon: Double,
    val heading: Float,
    val confidence: Float,
) {
    val point: GeoPoint get() = GeoPoint(lat, lon)
}

/**
 * '말한 곳' 스냅샷 (기술서 7-1 Capture). 버튼을 누른 순간 고정하고 그 뒤 실시간 위치는 다시 읽지 않는다.
 * [current]가 null 이면 '위치 없음'.
 */
data class LocationSnapshot(
    val id: Long,
    val pressedAtMs: Long,
    val current: LocationSample?,
    val history: List<LocationSample>,
    val source: PlaceSource = PlaceSource.VOICE,
) {
    val hasLocation: Boolean get() = current != null
}
