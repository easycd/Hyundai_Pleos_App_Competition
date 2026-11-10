package com.easycd.nextplace.core.domain.voice

/** 발화 정규화: 문장부호 제거, 공백 하나로. */
object TextNormalizer {
    private val punct = Regex("[\\p{Punct}“”‘’·…~!?.,]")
    private val spaces = Regex("\\s+")

    fun normalize(text: String): String = text.replace(punct, " ").replace(spaces, " ").trim()

    /** 이름 비교용: 공백까지 모두 뺀 소문자. */
    fun compact(text: String): String = normalize(text).replace(" ", "").lowercase()
}
