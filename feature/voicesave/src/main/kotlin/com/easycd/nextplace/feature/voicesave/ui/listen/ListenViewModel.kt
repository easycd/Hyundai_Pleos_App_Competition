package com.easycd.nextplace.feature.voicesave.ui.listen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.core.domain.sdk.DevVoiceInput
import com.easycd.nextplace.feature.voicesave.pipeline.SaveFlowState
import com.easycd.nextplace.feature.voicesave.pipeline.SavePipeline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ListenViewModel @Inject constructor(
    private val pipeline: SavePipeline,
    private val dev: DevVoiceInput,
) : ViewModel() {
    val state: StateFlow<ListenUiState> = pipeline.state.map { s ->
        when (s) {
            is SaveFlowState.Listening -> ListenUiState(ListenPhase.LISTENING, s.partial, s.level, devInput = dev.enabled)
            is SaveFlowState.Understanding -> ListenUiState(ListenPhase.UNDERSTANDING, s.text)
            SaveFlowState.Retry -> ListenUiState(ListenPhase.RETRY)
            is SaveFlowState.Searching -> ListenUiState(ListenPhase.SEARCHING, s.text, target = s.target, current = s.current)
            else -> ListenUiState()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ListenUiState())

    fun onAction(a: ListenAction) {
        when (a) {
            ListenAction.Cancel -> pipeline.cancel()
            is ListenAction.DevSubmit -> if (a.text.isNotBlank()) dev.submit(a.text)
        }
    }
}
