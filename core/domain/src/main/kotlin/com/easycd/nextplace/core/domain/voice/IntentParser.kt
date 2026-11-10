package com.easycd.nextplace.core.domain.voice

import javax.inject.Inject

/**
 * 의도 판정 (기획서 6장, 기술서 7-0 '의도 판정'). 지금 단계에서 받는 의도만 고르고 CANCEL을 가장 먼저 본다.
 * 저장 발화의 단서는 [SlotExtractor]가 뽑는다.
 */
class IntentParser @Inject constructor(private val slots: SlotExtractor) {

    /** [choices]는 추천 단계의 카드 이름 목록 (번호 순). */
    fun parse(raw: String, stage: VoiceStage, choices: List<String> = emptyList()): ParsedUtterance {
        val text = TextNormalizer.normalize(raw)
        if (text.isEmpty()) return ParsedUtterance(raw, Intent.UNKNOWN)
        return when (stage) {
            VoiceStage.HOME -> home(raw, text)
            VoiceStage.CONFIRM, VoiceStage.REMIND -> yesNo(raw, text, stage)
            VoiceStage.RECOMMEND -> select(raw, text, choices)
            VoiceStage.MEMO -> memo(raw, text)
        }
    }

    // 홈: CANCEL → RECOMMEND → NOTE → SAVE, 그 밖은 SAVE
    private fun home(raw: String, text: String): ParsedUtterance {
        if (isCancel(text)) return ParsedUtterance(raw, Intent.CANCEL)
        val cues = slots.extract(raw)
        val save = hasSaveVerb(text)
        val strongRecommend = STRONG_RECOMMEND_WORDS.any { text.contains(it) }
        val weakRecommend = WEAK_RECOMMEND_WORDS.any { text.contains(it) }
        val intent = when {
            strongRecommend || (weakRecommend && !save) -> Intent.RECOMMEND
            !save && NOTE_WORDS.any { text.contains(it) } -> Intent.NOTE
            else -> Intent.SAVE
        }
        return ParsedUtterance(raw, intent, cues)
    }

    // 확인·리마인드: CANCEL → YES · NO, 그 밖은 UNKNOWN(무응답과 같음). 리마인드의 CANCEL은 NO와 같다.
    private fun yesNo(raw: String, text: String, stage: VoiceStage): ParsedUtterance {
        if (isCancel(text)) return ParsedUtterance(raw, if (stage == VoiceStage.REMIND) Intent.NO else Intent.CANCEL)
        val tokens = text.split(' ')
        val no = NO_WORDS.any { matchWord(tokens, text, it) }
        val yes = YES_WORDS.any { matchWord(tokens, text, it) } || hasSaveVerb(text) || text.contains("가자")
        val intent = when {
            no -> Intent.NO
            yes -> Intent.YES
            else -> Intent.UNKNOWN
        }
        return ParsedUtterance(raw, intent)
    }

    // 추천: CANCEL → SELECT · NEXT
    private fun select(raw: String, text: String, choices: List<String>): ParsedUtterance {
        if (isCancel(text)) return ParsedUtterance(raw, Intent.CANCEL)
        if (NEXT_WORDS.any { text.contains(it) }) return ParsedUtterance(raw, Intent.NEXT)
        number(text)?.let { return ParsedUtterance(raw, Intent.SELECT, selectedIndex = it - 1) }
        val compact = TextNormalizer.compact(text)
        val hits = choices.withIndex().filter { (_, name) ->
            val n = TextNormalizer.compact(name)
            n.isNotEmpty() && (compact.contains(n) || (compact.length >= 2 && n.contains(compact)))
        }
        return when {
            hits.size == 1 -> ParsedUtterance(raw, Intent.SELECT, selectedIndex = hits[0].index, selectedByName = true)
            hits.size > 1 -> ParsedUtterance(raw, Intent.SELECT, ambiguous = true, selectedByName = true)
            else -> ParsedUtterance(raw, Intent.UNKNOWN)
        }
    }

    // 방문 메모: "됐어"·"괜찮아요"만 취소, 그 밖은 모두 메모
    private fun memo(raw: String, text: String): ParsedUtterance {
        val compact = TextNormalizer.compact(text)
        val cancel = MEMO_CANCEL_EXACT.contains(compact) || CANCEL_WORDS.any { text.contains(it) }
        return ParsedUtterance(raw, if (cancel) Intent.CANCEL else Intent.MEMO)
    }

    private fun isCancel(text: String): Boolean = CANCEL_WORDS.any { text.contains(it) } ||
        TextNormalizer.compact(text).let { c -> CANCEL_EXACT.any { c == it } }

    /** 띄어 쓴 말은 문장 안에서, 한 글자 말은 단어 그대로(또는 '요'를 붙여), 나머지는 단어 시작으로 찾는다. */
    private fun matchWord(tokens: List<String>, text: String, w: String): Boolean = when {
        w.contains(' ') -> text.contains(w)
        w.length == 1 -> tokens.any { t -> t == w || t == w + "요" || (t.isNotEmpty() && t.all { it == w[0] }) }
        else -> tokens.any { it.startsWith(w) }
    }

    /** '저장한'처럼 꾸미는 말은 저장이 아니다. */
    private fun hasSaveVerb(text: String): Boolean {
        val cleaned = text.replace("저장한", " ").replace("저장된", " ").replace("기억한", " ")
        return SAVE_WORDS.any { cleaned.contains(it) }
    }

    /** "2번", "두 번째", "둘째" → 2. 없으면 null. */
    private fun number(text: String): Int? {
        DIGIT_NUMBER.find(text)?.let { return it.groupValues[1].toIntOrNull() }
        val compact = text.replace(" ", "")
        return ORDINALS.entries.firstOrNull { (words, _) -> words.any { compact.contains(it) } }?.value
    }

    private companion object {
        val CANCEL_WORDS = listOf("취소", "그만", "하지 마", "하지마", "됐어", "됐다", "필요 없", "필요없")
        val CANCEL_EXACT = listOf("됐어요", "아냐됐어")
        val SAVE_WORDS = listOf("저장", "기억해", "기억하", "찍어", "찍자", "다음에", "남겨", "메모해", "적어")
        val STRONG_RECOMMEND_WORDS = listOf("먹을까", "추천", "뭐 먹지", "뭐먹지", "어디 갈까", "배고파")
        val WEAK_RECOMMEND_WORDS = listOf("찾아", "근처", "어디 있")
        val NOTE_WORDS = listOf("괜찮", "맛있", "별로", "다시는", "다신", "맛없")
        val YES_WORDS = listOf("응", "어", "네", "예", "맞아", "맞습니다", "맞네", "맞고", "그래", "좋아", "오케이", "콜", "갈래", "가줘", "고고", "그렇")
        val NO_WORDS = listOf("아니", "아뇨", "다른", "틀려", "틀렸", "싫어", "안 가", "안가", "노")
        val NEXT_WORDS = listOf("다른", "다음 거", "다음거", "또 없", "더 없")
        val MEMO_CANCEL_EXACT = setOf("됐어", "됐어요", "괜찮아", "괜찮아요", "아니", "아니요", "아뇨", "없어", "없어요", "다음에")
        // 여러 자리 숫자를 통째로 읽는다 ("11번"이 1번으로 잡히지 않게). 범위 밖 번호는 대화 쪽에서 거른다.
        val DIGIT_NUMBER = Regex("(?<![0-9])([0-9]+)\\s?번")
        val ORDINALS = linkedMapOf(
            listOf("첫번째", "첫째", "일번", "하나", "첫번") to 1,
            listOf("두번째", "둘째", "이번째", "두번", "둘") to 2,
            listOf("세번째", "셋째", "삼번", "세번", "셋") to 3,
        )
    }
}
