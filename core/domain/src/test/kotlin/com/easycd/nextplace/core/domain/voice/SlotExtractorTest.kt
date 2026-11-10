package com.easycd.nextplace.core.domain.voice

import com.easycd.nextplace.core.domain.model.DirectionCue
import com.easycd.nextplace.core.domain.model.TimeCue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** 기획서 6장 '저장 발화에서 뽑는 단서' 표. */
class SlotExtractorTest {
    private val slots = SlotExtractor()

    @Test
    fun fullSentence() {
        val c = slots.extract("방금 지난 빨간 간판 고깃집 저장해줘")
        assertEquals("고깃집", c.category)
        assertEquals(TimeCue.JUST, c.time)
        assertTrue(c.appearance.contains("빨간 간판"))
    }

    @Test
    fun timeCues() {
        assertEquals(TimeCue.EARLIER, slots.extract("아까 그 카페").time)
        assertEquals(TimeCue.HERE, slots.extract("여기 저장").time)
        assertEquals(TimeCue.NONE, slots.extract("국밥집 저장").time)
    }

    @Test
    fun directionCues() {
        assertEquals(DirectionCue.RIGHT, slots.extract("오른쪽에 저 카페 기억해").direction)
        assertEquals(DirectionCue.LEFT, slots.extract("왼쪽 빵집").direction)
        assertEquals(DirectionCue.OPPOSITE, slots.extract("건너편 국밥집").direction)
        assertEquals(DirectionCue.BOTH, slots.extract("국밥집").direction)
    }

    @Test
    fun appearanceFloor() {
        assertTrue(slots.extract("하얀 건물 2층 파스타집").appearance.containsAll(listOf("하얀 건물", "2층")))
    }

    @Test
    fun nameHint() {
        assertEquals("서울", slots.extract("서울 뭐시기 고깃집").nameHint)
        assertEquals("할머니", slots.extract("할머니 어쩌고 국밥").nameHint)
        assertNull(slots.extract("방금 카페").nameHint)
    }

    @Test
    fun memo() {
        assertEquals("줄 서 있던", slots.extract("줄 서 있던 빵집 저장").memo)
        assertEquals("유튜브에 나온", slots.extract("유튜브에 나온 국밥집").memo)
    }
}
