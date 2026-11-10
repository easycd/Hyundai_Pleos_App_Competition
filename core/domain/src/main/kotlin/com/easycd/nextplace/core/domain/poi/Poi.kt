package com.easycd.nextplace.core.domain.poi

import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.voice.Category

/** POI 검색 조건. [keyword]는 이름 일부, [category]는 업종. 둘 다 없으면 음식점·카페 전체. */
data class PoiQuery(
    val center: GeoPoint,
    val radiusM: Int,
    val category: Category? = null,
    val keyword: String? = null,
)

sealed interface PoiResult {
    /** [stale] = 오프라인이라 7일 안의 오래된 캐시를 씀. */
    data class Found(val candidates: List<PoiCandidate>, val stale: Boolean = false) : PoiResult
    data object Offline : PoiResult
    data object Failed : PoiResult
}

/** 가게 찾기 (카카오 로컬 API 또는 내장 샘플). 카카오 ID는 내비로 넘기지 않는다. */
interface PoiSource {
    suspend fun search(query: PoiQuery): PoiResult
}

/** 좌표 → 도로명 (홈 현재 위치, 30초에 1회). */
interface ReverseGeocoder {
    suspend fun roadName(point: GeoPoint): String?
}

/** POI 응답 캐시 저장소 (기술서 8장 poi_cache 표, 구현은 core/data). */
interface PoiCacheStore {
    suspend fun get(key: String): CachedPoi?
    suspend fun put(key: String, json: String, savedAt: Long)
    suspend fun clear()
}

data class CachedPoi(val json: String, val savedAt: Long)
