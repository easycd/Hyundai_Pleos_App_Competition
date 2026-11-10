package com.easycd.nextplace.feature.recommend.session

import com.easycd.nextplace.core.domain.voice.VoicePrompt

data class RecCard(val id: String, val number: Int, val name: String, val category: String?, val distanceM: Double?, val memo: String?)

/** 추천 세션 상태 하나 (S-06 · S-06b · S-06c). */
sealed interface RecommendState {
    data object Closed : RecommendState
    data object Loading : RecommendState

    data class Showing(
        val category: String?,
        val cards: List<RecCard>,
        val page: Int,
        val readingIndex: Int? = null,
        val selectedIndex: Int? = null,
    ) : RecommendState

    data class Empty(val category: String?, val reason: VoicePrompt.NoneReason) : RecommendState
}
