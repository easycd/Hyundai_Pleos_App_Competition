package com.easycd.nextplace.core.domain.voice

import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.DirectionCue
import com.easycd.nextplace.core.domain.model.TimeCue
import javax.inject.Inject

/** 저장 발화에서 단서(슬롯)를 뽑는다 (기획서 6장 '저장 발화에서 뽑는 단서'). 규칙 기반, 네트워크 없음. */
class SlotExtractor @Inject constructor() {

    fun extract(raw: String): Cues {
        val text = TextNormalizer.normalize(raw)
        return Cues(
            category = Categories.find(text)?.name,
            time = time(text),
            direction = direction(text),
            appearance = appearance(text),
            nameHint = nameHint(text),
            memo = memo(text),
        )
    }

    private fun time(text: String): TimeCue = when {
        HERE.any { text.contains(it) } -> TimeCue.HERE
        EARLIER.any { text.contains(it) } -> TimeCue.EARLIER
        JUST.any { text.contains(it) } -> TimeCue.JUST
        else -> TimeCue.NONE
    }

    private fun direction(text: String): DirectionCue = when {
        OPPOSITE.any { text.contains(it) } -> DirectionCue.OPPOSITE
        RIGHT.any { text.contains(it) } -> DirectionCue.RIGHT
        LEFT.any { text.contains(it) } -> DirectionCue.LEFT
        else -> DirectionCue.BOTH
    }

    private fun appearance(text: String): List<String> {
        val out = mutableListOf<String>()
        val tokens = text.split(' ')
        tokens.forEachIndexed { i, tok ->
            if (COLORS.any { tok.startsWith(it) }) {
                val next = tokens.getOrNull(i + 1)
                out += if (next != null && SHAPES.any { next.startsWith(it) }) "$tok $next" else tok
            }
        }
        FLOOR.find(text)?.let { out += it.value }
        return out.distinct()
    }

    private fun nameHint(text: String): String? {
        VAGUE.find(text)?.let { m -> return m.groupValues[1].trim().takeIf { it.isNotEmpty() } }
        CALLED.find(text)?.let { m -> return m.groupValues[1].trim().takeIf { it.length >= 2 } }
        return null
    }

    private fun memo(text: String): String? = MEMO_PATTERNS.firstNotNullOfOrNull { it.find(text)?.value?.trim() }

    private companion object {
        val HERE = listOf("여기", "이 집", "이집", "지금 이")
        val EARLIER = listOf("아까", "좀 전", "조금 전", "아까 전")
        val JUST = listOf("방금", "지금 막", "막 지난", "금방")
        val OPPOSITE = listOf("건너편", "맞은편", "반대편", "길 건너")
        val RIGHT = listOf("오른쪽", "오른편", "우측")
        val LEFT = listOf("왼쪽", "왼편", "좌측")
        val COLORS = listOf("빨간", "빨강", "파란", "파랑", "노란", "노랑", "하얀", "흰", "검은", "까만", "초록", "주황", "분홍", "보라", "갈색")
        val SHAPES = listOf("간판", "건물", "지붕", "집", "가게", "문", "벽")
        val FLOOR = Regex("[0-9일이삼사오]\\s?층")
        val VAGUE = Regex("([가-힣A-Za-z0-9]+(?:\\s[가-힣A-Za-z0-9]+)?)\\s?(?:뭐시기|어쩌고|뭐더라|뭐라나|머시기|거시기)")
        val CALLED = Regex("([가-힣A-Za-z0-9]+?)(?:이라는|라는|이란|란)\\s")
        val MEMO_PATTERNS = listOf(
            Regex("줄[^ ]* 서[^ ]*( 있던| 있는)?"),
            Regex("(유튜브|티비|TV|방송)에? 나온"),
            Regex("사람 (많|붐비)[^ ]*"),
            Regex("웨이팅[^ ]*( 있던| 있는)?"),
        )
    }
}
