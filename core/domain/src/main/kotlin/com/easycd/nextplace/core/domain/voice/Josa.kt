package com.easycd.nextplace.core.domain.voice

import com.easycd.nextplace.core.domain.Tunables

/** 한국어 조사 고르기와 TTS 이름 자르기 (기획서 6장 규칙 5: 가게 이름은 앞 12자까지만 읽음). */
object Josa {
    private const val HANGUL_BASE = 0xAC00
    private const val HANGUL_END = 0xD7A3
    private const val JONG_COUNT = 28
    private const val RIEUL = 8

    private fun jong(word: String): Int? {
        val c = word.trimEnd().lastOrNull() ?: return null
        if (c.code !in HANGUL_BASE..HANGUL_END) return if (c.isDigit()) digitJong(c) else null
        return (c.code - HANGUL_BASE) % JONG_COUNT
    }

    private fun digitJong(c: Char): Int = when (c) {
        '0', '1', '3', '6', '7', '8' -> 1
        else -> 0
    }

    /** 이/가 */
    fun iGa(word: String) = word + if ((jong(word) ?: 0) == 0) "가" else "이"

    /** 을/를 */
    fun eulReul(word: String) = word + if ((jong(word) ?: 0) == 0) "를" else "을"

    /** 으로/로 (받침 없음·ㄹ받침이면 '로') */
    fun ro(word: String): String {
        val j = jong(word) ?: 0
        return word + if (j == 0 || j == RIEUL) "로" else "으로"
    }

    /** 에 */
    fun e(word: String) = word + "에"

    fun spokenName(name: String): String = name.take(Tunables.TTS_NAME_MAX)
}
