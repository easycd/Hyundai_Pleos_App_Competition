package com.easycd.nextplace.feature.voicesave.rank

import com.easycd.nextplace.core.domain.location.LocatedPoint
import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.DirectionCue
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.PoiCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 기술서 7-1 6단계: 업종 40 · 이름 30 · 거리 20 · 좌우 10, 확실 = 업종 일치 + 2위와 30점 이상 차이. */
class CandidateScorerTest {
    private val scorer = CandidateScorer()

    // 북쪽으로 달리는 차. 동쪽(+lon)이 오른쪽.
    private val here = LocatedPoint(GeoPoint(37.5, 127.0), headingDeg = 0.0, targetTimeMs = 0, timeInexact = false)

    private fun poi(id: String, name: String, category: String, dLat: Double, dLon: Double) =
        PoiCandidate("kakao", id, name, category, null, 37.5 + dLat, 127.0 + dLon)

    @Test
    fun categoryAndSideWin() {
        val list = listOf(
            poi("1", "온도커피", "음식점 > 카페", 0.0002, 0.0003),
            poi("2", "서울갈비", "음식점 > 한식 > 육류,고기", 0.0002, 0.0003),
            poi("3", "왼쪽갈비", "음식점 > 한식 > 육류,고기", 0.0002, -0.0003),
        )
        val ranked = scorer.rank(list, here, Cues(category = "고깃집", direction = DirectionCue.RIGHT))
        assertEquals("서울갈비", ranked.first().name)
        assertEquals("왼쪽갈비", ranked[1].name)
        assertTrue(ranked.first().score > ranked[1].score)
    }

    @Test
    fun nameHintAddsThirty() {
        val list = listOf(
            poi("1", "할머니국밥", "음식점 > 한식 > 국밥", 0.0005, 0.0),
            poi("2", "부산국밥", "음식점 > 한식 > 국밥", 0.0001, 0.0),
        )
        val ranked = scorer.rank(list, here, Cues(category = "국밥", nameHint = "할머니"))
        assertEquals("할머니국밥", ranked.first().name)
    }

    @Test
    fun certainOnlyWithCategoryAndGap() {
        val list = listOf(
            poi("1", "서울갈비", "음식점 > 한식 > 육류,고기", 0.0001, 0.0001),
            poi("2", "온도커피", "음식점 > 카페", 0.0001, 0.0001),
        )
        val cues = Cues(category = "고깃집")
        assertTrue(scorer.isCertain(scorer.rank(list, here, cues), cues))
        assertFalse(scorer.isCertain(scorer.rank(list, here, Cues()), Cues()))
    }

    @Test
    fun keepsAtMostFive() {
        val list = (1..9).map { poi("$it", "가게$it", "음식점", it * 0.0001, 0.0) }
        assertEquals(5, scorer.rank(list, here, Cues()).size)
    }
}
