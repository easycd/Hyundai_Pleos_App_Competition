package com.easycd.nextplace.feature.visit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.feature.visit.note.VisitNoteAsk
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VisitNoteViewModel @Inject constructor(private val ask: VisitNoteAsk) : ViewModel() {
    val state: StateFlow<VisitNoteUiState> = ask.state.map { VisitNoteUiState(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, VisitNoteUiState())

    fun onAction(a: VisitNoteAction) = ask.press(a == VisitNoteAction.Speak)
}
