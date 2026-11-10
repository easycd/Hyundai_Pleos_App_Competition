package com.easycd.nextplace.core.domain.model

/** POI 검색 결과 1건과 매칭 점수 (기획서 7장 Place.candidates). */
data class PoiCandidate(
    val provider: String,
    val providerId: String,
    val name: String,
    val category: String?,
    val address: String?,
    val lat: Double,
    val lon: Double,
    val distanceM: Double = 0.0,
    val side: Side = Side.UNKNOWN,
    val score: Double = 0.0,
)
