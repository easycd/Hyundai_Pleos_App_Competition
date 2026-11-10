package com.easycd.nextplace.feature.remind.pipeline

import com.easycd.nextplace.core.domain.model.Side

import com.easycd.nextplace.core.domain.voice.VoicePrompt.SavedWhen

/** S-05 리마인드 · S-05b 안내 시작 카드 상태. */
sealed interface RemindState {
    data object Hidden : RemindState

    data class Asking(
        val name: String,
        val savedWhen: SavedWhen,
        val distanceM: Int,
        val ahead: Boolean,
        val side: Side,
        val waitStartedAt: Long?,
    ) : RemindState

    data class Routing(val name: String, val changing: Boolean) : RemindState
}
