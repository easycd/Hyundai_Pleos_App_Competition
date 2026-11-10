package com.easycd.nextplace.feature.recommend.score.rule

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.model.PlaceWithVisits
import javax.inject.Inject

/** 경과 25: 방문한 적 없으면 25, 있으면 25 × min(1, 마지막 방문 후 일수/30). */
class LastVisitRule @Inject constructor() : RecommendRule {
    override fun score(p: PlaceWithVisits, ctx: RecommendContext): Double {
        val last = p.lastVisitAt ?: return Tunables.RECOMMEND_W_LAST_VISIT
        val days = (ctx.nowMs - last).toDouble() / DAY_MS
        return Tunables.RECOMMEND_W_LAST_VISIT * minOf(1.0, days / Tunables.RECOMMEND_VISIT_FULL_DAYS)
    }

    private companion object {
        const val DAY_MS = 86_400_000.0
    }
}
