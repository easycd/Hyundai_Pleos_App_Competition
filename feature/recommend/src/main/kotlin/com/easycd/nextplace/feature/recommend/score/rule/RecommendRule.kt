package com.easycd.nextplace.feature.recommend.score.rule

import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.PlaceWithVisits

/** 추천 점수 규칙 하나 (기술서 6장 확장 지점). 새 규칙은 클래스 하나 + RecommendScorer 목록 한 줄. */
interface RecommendRule {
    fun score(p: PlaceWithVisits, ctx: RecommendContext): Double
}

data class RecommendContext(val origin: GeoPoint?, val radiusM: Int, val nowMs: Long)
