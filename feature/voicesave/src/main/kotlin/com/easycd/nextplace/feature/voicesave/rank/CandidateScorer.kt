package com.easycd.nextplace.feature.voicesave.rank

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.location.LocatedPoint
import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.DirectionCue
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.model.Side
import com.easycd.nextplace.core.domain.voice.Categories
import com.easycd.nextplace.core.domain.voice.TextNormalizer
import javax.inject.Inject
import kotlin.math.max

/**
 * 6단계 점수 (기술서 7-1): 업종 일치 40 · 이름 일부 30 · 거리 20 · 좌우 10 = 100점.
 * 좌우는 방향을 말했고 맞으면 10, 말하지 않았거나 판정 불가면 5, 반대면 0. 건너편은 왼쪽으로 본다.
 */
class CandidateScorer @Inject constructor() {

    fun rank(candidates: List<PoiCandidate>, located: LocatedPoint, cues: Cues): List<PoiCandidate> =
        candidates.map { score(it, located, cues) }
            .sortedWith(compareByDescending<PoiCandidate> { it.score }.thenBy { it.distanceM })
            .take(Tunables.KEEP_CANDIDATES)

    /** 1순위가 확실한가: 2순위보다 30점 이상 높고 업종 일치 → 되묻지 않고 저장. */
    fun isCertain(ranked: List<PoiCandidate>, cues: Cues): Boolean {
        val top = ranked.firstOrNull() ?: return false
        val second = ranked.getOrNull(1)?.score ?: 0.0
        return categoryMatches(top, cues) && top.score - second >= Tunables.SCORE_CERTAIN_GAP
    }

    fun categoryMatches(c: PoiCandidate, cues: Cues): Boolean {
        val cat = Categories.byName(cues.category) ?: return false
        return Categories.matches(cat, c.category)
    }

    private fun score(c: PoiCandidate, located: LocatedPoint, cues: Cues): PoiCandidate {
        val point = GeoPoint(c.lat, c.lon)
        val distance = Geo.distanceM(located.point, point)
        val side = Geo.sideOf(located.point, located.headingDeg, point)
        var s = 0.0
        if (categoryMatches(c, cues)) s += Tunables.SCORE_CATEGORY
        if (nameMatches(c.name, cues.nameHint)) s += Tunables.SCORE_NAME
        s += Tunables.SCORE_DISTANCE * max(0.0, 1.0 - distance / Tunables.POI_RADIUS_M)
        s += sideScore(cues.direction, side)
        return c.copy(distanceM = distance, side = side, score = s)
    }

    private fun nameMatches(name: String, hint: String?): Boolean {
        val h = hint?.let(TextNormalizer::compact)?.takeIf { it.length >= MIN_NAME } ?: return false
        val n = TextNormalizer.compact(name)
        return n.contains(h) || h.contains(n)
    }

    private fun sideScore(cue: DirectionCue, side: Side): Double {
        val wanted = when (cue) {
            DirectionCue.LEFT, DirectionCue.OPPOSITE -> Side.LEFT
            DirectionCue.RIGHT -> Side.RIGHT
            DirectionCue.BOTH -> return Tunables.SCORE_SIDE_UNKNOWN
        }
        return when (side) {
            Side.UNKNOWN -> Tunables.SCORE_SIDE_UNKNOWN
            wanted -> Tunables.SCORE_SIDE
            else -> 0.0
        }
    }

    private companion object {
        const val MIN_NAME = 2
    }
}
