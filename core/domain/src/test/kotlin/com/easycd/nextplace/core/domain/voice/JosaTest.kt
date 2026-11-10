package com.easycd.nextplace.core.domain.voice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JosaTest {
    @Test
    fun particles() {
        assertEquals("서울갈비가", Josa.iGa("서울갈비"))
        assertEquals("할머니국밥이", Josa.iGa("할머니국밥"))
        assertEquals("카페로", Josa.ro("카페"))
        assertEquals("서울식당으로", Josa.ro("서울식당"))
        assertEquals("을지로로", Josa.ro("을지로"))
        assertEquals("국밥을", Josa.eulReul("국밥"))
    }
}
