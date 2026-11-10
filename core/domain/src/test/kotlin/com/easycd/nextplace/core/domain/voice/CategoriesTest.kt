package com.easycd.nextplace.core.domain.voice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CategoriesTest {
    @Test fun mostSpecificPartWins() {
        // 에뮬레이터 점검: 삼계탕집이 '육류,고기' 칸 때문에 고깃집으로 저장됐다
        assertEquals("삼계탕", Categories.label("음식점 > 한식 > 육류,고기 > 닭요리 > 삼계탕", "고깃집"))
        assertEquals("고깃집", Categories.label("음식점 > 한식 > 육류,고기 > 갈비"))
        assertEquals("카페", Categories.label("음식점 > 카페 > 커피전문점 > 스타벅스"))
        assertEquals("국밥", Categories.label("음식점 > 한식 > 국밥"))
        assertEquals("파스타", Categories.label("음식점 > 양식 > 이탈리안"))
    }

    @Test fun saidCategoryBreaksTies() {
        assertEquals("치킨", Categories.label("음식점 > 한식 > 육류,고기 > 닭요리 > 닭갈비", "치킨"))
    }

    @Test fun fallsBackToSaidThenLastPart() {
        assertEquals("고깃집", Categories.label("음식점 > 기타", "고깃집"))
        assertEquals("기타", Categories.label("음식점 > 기타"))
        assertEquals("고깃집", Categories.label(null, "고깃집"))
        assertEquals(null as String?, Categories.label(null))
    }

    @Test fun findsSamgyetangFromSpeech() {
        assertEquals("삼계탕", Categories.find("방금 지난 삼계탕집 저장해줘")?.name)
        assertEquals("고깃집", Categories.find("방금 지난 고깃집 저장해줘")?.name)
    }
}
