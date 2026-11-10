package com.easycd.nextplace.feature.recommend.score

import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.PlaceWithVisits
import com.easycd.nextplace.core.domain.voice.Categories
import com.easycd.nextplace.feature.recommend.score.rule.DistanceRule
import com.easycd.nextplace.feature.recommend.score.rule.LastVisitRule
import com.easycd.nextplace.feature.recommend.score.rule.MemoRule
import com.easycd.nextplace.feature.recommend.score.rule.RecommendContext
import com.easycd.nextplace.feature.recommend.score.rule.RecommendRule
import javax.inject.Inject

data class Scored(val item: PlaceWithVisits, val score: Double, val distanceM: Double?)

/**
 * 추천 점수 = 규칙 3개의 합 100점 (기술서 7-3): 거리 50 · 메모 25 · 경과 25.
 * 동점이면 가까운 순, 그다음 최근 저장 순. 업종을 말했으면 그 업종만, 부정 메모는 뺀다.
 */
class RecommendScorer @Inject constructor(distance: DistanceRule, memo: MemoRule, lastVisit: LastVisitRule) {
    private val rules: List<RecommendRule> = listOf(distance, memo, lastVisit)

    fun rank(items: List<PlaceWithVisits>, category: String?, ctx: RecommendContext): List<Scored> {
        val cat = Categories.byName(category)
        return items.asSequence()
            .filter { it.place.deletedAt == null && !MemoRule.isNegative(it) }
            .filter { cat == null || it.place.category == cat.name || Categories.matches(cat, it.place.category) }
            .map { p ->
                val d = ctx.origin?.let { o -> p.place.point?.let { Geo.distanceM(o, it) } }
                Scored(p, rules.sumOf { it.score(p, ctx) }, d)
            }
            .sortedWith(
                compareByDescending<Scored> { it.score }
                    .thenBy { it.distanceM ?: Double.MAX_VALUE }
                    .thenByDescending { it.item.place.savedAt },
            )
            .toList()
    }
}
