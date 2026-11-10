package com.easycd.nextplace.core.domain.voice

import com.easycd.nextplace.core.domain.model.Cues

/** 의도 (기획서 6장). */
enum class Intent { SAVE, RECOMMEND, NOTE, YES, NO, CANCEL, SELECT, NEXT, MEMO, UNKNOWN }

/** 지금 단계. 같은 말도 단계에 따라 뜻이 다르다 (기획서 6장 '지금 단계에서 받는 의도'). */
enum class VoiceStage { HOME, CONFIRM, RECOMMEND, REMIND, MEMO }

/** 의도 판정 결과. [selectedIndex]는 SELECT 일 때 0부터. */
data class ParsedUtterance(
    val text: String,
    val intent: Intent,
    val cues: Cues = Cues(),
    val selectedIndex: Int? = null,
    val selectedByName: Boolean = false,
    val ambiguous: Boolean = false,
)
