package com.easycd.nextplace.feature.visit.ui

sealed interface VisitNoteAction {
    data object Speak : VisitNoteAction
    data object Later : VisitNoteAction
}
