package com.easycd.nextplace.feature.visit.note

/** S-15 방문 메모 카드 상태. */
sealed interface VisitNoteState {
    data object Hidden : VisitNoteState
    data class Asking(
        val placeName: String,
        val visitCount: Int,
        val waitStartedAt: Long?,
        val listening: Boolean,
        val partial: String,
    ) : VisitNoteState
}
