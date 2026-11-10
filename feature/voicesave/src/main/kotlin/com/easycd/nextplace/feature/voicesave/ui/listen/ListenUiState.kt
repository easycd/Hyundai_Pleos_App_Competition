package com.easycd.nextplace.feature.voicesave.ui.listen

import com.easycd.nextplace.core.domain.model.GeoPoint

enum class ListenPhase { HIDDEN, LISTENING, UNDERSTANDING, RETRY, SEARCHING }

/** S-02a 듣는 중 · S-02b 알아듣는 중 · S-02c 다시 말해 주세요 · S-14 가게 찾는 중. */
data class ListenUiState(
    val phase: ListenPhase = ListenPhase.HIDDEN,
    val text: String = "",
    val level: Float = 0f,
    val target: GeoPoint? = null,
    val current: GeoPoint? = null,
    val devInput: Boolean = false,
)
