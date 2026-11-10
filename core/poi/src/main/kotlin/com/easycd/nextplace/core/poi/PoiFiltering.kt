package com.easycd.nextplace.core.poi

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.poi.PoiQuery
import com.easycd.nextplace.core.domain.voice.Categories
import com.easycd.nextplace.core.domain.voice.TextNormalizer

/** 받은 후보를 실제 기준점에서 반경으로 다시 거르고 거리순으로 자른다 (칸 경계 누락 방지). */
internal fun List<PoiCandidate>.refine(query: PoiQuery): List<PoiCandidate> =
    map { it.copy(distanceM = Geo.distanceM(query.center, GeoPoint(it.lat, it.lon))) }
        .filter { it.distanceM <= query.radiusM }
        .sortedBy { it.distanceM }
        .take(Tunables.POI_MAX_CANDIDATES)

/** 내장 샘플·캐시용 이름·업종 필터. 이름 일부가 있으면 이름으로, 아니면 업종으로. */
internal fun List<PoiCandidate>.matching(query: PoiQuery): List<PoiCandidate> {
    val key = query.keyword?.let(TextNormalizer::compact)?.takeIf { it.isNotEmpty() }
    val cat = query.category
    return filter { c ->
        val nameOk = key == null || TextNormalizer.compact(c.name).contains(key) || key.contains(TextNormalizer.compact(c.name))
        val catOk = cat == null || key != null || Categories.matches(cat, c.category)
        nameOk && catOk
    }
}
