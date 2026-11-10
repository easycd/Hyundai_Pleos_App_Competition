package com.easycd.nextplace.core.domain.voice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/** 기획서 6장 '의도와 대표 발화' · '지금 단계에서 받는 의도' 표를 그대로 검사한다. */
class IntentParserTest {
    private val parser = IntentParser(SlotExtractor())

    @ParameterizedTest(name = "홈 \"{0}\" → {1}")
    @CsvSource(
        "방금 지난 빨간 간판 고깃집 저장해줘, SAVE",
        "오른쪽에 저 카페 기억해, SAVE",
        "여기 다음에 와보자, SAVE",
        "뭐 먹을까, RECOMMEND",
        "근처에 저장한 데 있어?, RECOMMEND",
        "저장한 고깃집 찾아줘, RECOMMEND",
        "여기 괜찮았어, NOTE",
        "맛있었어, NOTE",
        "다시는 안 와, NOTE",
        "취소, CANCEL",
        "그만, CANCEL",
        "저장하지 마, CANCEL",
        "됐어, CANCEL",
        "저기 국밥집, SAVE",
    )
    fun home(text: String, expected: Intent) {
        assertEquals(expected, parser.parse(text, VoiceStage.HOME).intent)
    }

    @ParameterizedTest(name = "확인 \"{0}\" → {1}")
    @CsvSource(
        "응, YES",
        "맞아, YES",
        "저장해, YES",
        "네 맞아요, YES",
        "아니, NO",
        "다른 데, NO",
        "취소, CANCEL",
        "글쎄, UNKNOWN",
    )
    fun confirm(text: String, expected: Intent) {
        assertEquals(expected, parser.parse(text, VoiceStage.CONFIRM).intent)
    }

    @Test
    fun remindCancelIsNo() {
        assertEquals(Intent.NO, parser.parse("그만", VoiceStage.REMIND).intent)
        assertEquals(Intent.YES, parser.parse("응 가자", VoiceStage.REMIND).intent)
    }

    @ParameterizedTest(name = "추천 \"{0}\" → {1}")
    @CsvSource("2번, 1", "두 번째, 1", "첫 번째, 0", "세 번째 거, 2", "서울갈비, 0", "할머니국밥 갈래, 1")
    fun selectByNumberOrName(text: String, index: Int) {
        val r = parser.parse(text, VoiceStage.RECOMMEND, listOf("서울갈비", "할머니국밥", "카페 온도"))
        assertEquals(Intent.SELECT, r.intent)
        assertEquals(index, r.selectedIndex)
    }

    @Test
    fun selectNextAndCancel() {
        assertEquals(Intent.NEXT, parser.parse("다른 곳", VoiceStage.RECOMMEND).intent)
        assertEquals(Intent.CANCEL, parser.parse("취소", VoiceStage.RECOMMEND).intent)
        assertEquals(Intent.UNKNOWN, parser.parse("음", VoiceStage.RECOMMEND, listOf("서울갈비")).intent)
    }

    @Test
    fun ambiguousName() {
        val r = parser.parse("갈비", VoiceStage.RECOMMEND, listOf("서울갈비", "수원갈비"))
        assertTrue(r.ambiguous)
    }

    @ParameterizedTest(name = "메모 \"{0}\" → {1}")
    @CsvSource("됐어, CANCEL", "괜찮아요, CANCEL", "국물이 진했어, MEMO", "괜찮았어 또 올래, MEMO")
    fun memo(text: String, expected: Intent) {
        assertEquals(expected, parser.parse(text, VoiceStage.MEMO).intent)
    }

    @Test
    fun emptyIsUnknown() {
        assertEquals(Intent.UNKNOWN, parser.parse("  ", VoiceStage.HOME).intent)
    }
}
