package com.easycd.nextplace.feature.recommend.score.rule

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.model.PlaceWithVisits
import javax.inject.Inject

/** 메모 25: 긍정 메모나 '괜찮았어' 같은 방문 메모가 있으면 25, 없으면 12.5. 부정 메모는 후보에서 뺀다. */
class MemoRule @Inject constructor() : RecommendRule {
    override fun score(p: PlaceWithVisits, ctx: RecommendContext): Double =
        if (isPositive(p)) Tunables.RECOMMEND_W_MEMO else Tunables.RECOMMEND_W_MEMO / 2

    companion object {
        private val POSITIVE = listOf("괜찮", "맛있", "맛집", "좋았", "좋아", "최고", "또 가", "또가", "추천", "훌륭")
        private val NEGATIVE = listOf("다시는", "다신", "별로", "맛없", "최악", "비추")

        private fun texts(p: PlaceWithVisits) = listOfNotNull(p.place.memo, p.place.cues.memo) + p.visitNotes

        fun isPositive(p: PlaceWithVisits) = texts(p).any { t -> POSITIVE.any { t.contains(it) } && NEGATIVE.none { t.contains(it) } }

        fun isNegative(p: PlaceWithVisits) = texts(p).any { t -> NEGATIVE.any { t.contains(it) } }
    }
}
