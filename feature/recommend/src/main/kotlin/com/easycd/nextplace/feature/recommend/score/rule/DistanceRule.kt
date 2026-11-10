package com.easycd.nextplace.feature.recommend.score.rule

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.PlaceWithVisits
import javax.inject.Inject
import kotlin.math.max

/** 거리 50 × (1 − 거리/반경). 거리는 '말한 곳' 기준. */
class DistanceRule @Inject constructor() : RecommendRule {
    override fun score(p: PlaceWithVisits, ctx: RecommendContext): Double {
        val o = ctx.origin ?: return 0.0
        val pt = p.place.point ?: return 0.0
        return Tunables.RECOMMEND_W_DISTANCE * max(0.0, 1.0 - Geo.distanceM(o, pt) / ctx.radiusM)
    }
}
