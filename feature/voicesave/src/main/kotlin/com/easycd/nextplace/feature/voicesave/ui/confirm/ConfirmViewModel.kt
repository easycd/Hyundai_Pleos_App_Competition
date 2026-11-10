package com.easycd.nextplace.feature.voicesave.ui.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.feature.voicesave.pipeline.ConfirmAnswer
import com.easycd.nextplace.feature.voicesave.pipeline.SaveFlowState
import com.easycd.nextplace.feature.voicesave.pipeline.SavePipeline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConfirmViewModel @Inject constructor(private val pipeline: SavePipeline) : ViewModel() {
    val state: StateFlow<ConfirmUiState> = pipeline.state.map { s ->
        if (s !is SaveFlowState.Confirming) return@map ConfirmUiState()
        ConfirmUiState(
            visible = true,
            key = s.candidate.providerId,
            second = s.second,
            name = s.candidate.name,
            category = s.categoryLabel,
            distanceM = s.distanceFromSpokenM,
            ahead = s.ahead,
            side = s.candidate.side,
            address = s.candidate.address,
            waitStartedAt = s.waitStartedAt,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ConfirmUiState())

    fun onAction(a: ConfirmAction) {
        pipeline.answer(if (a == ConfirmAction.Yes) ConfirmAnswer.YES else ConfirmAnswer.NO)
    }
}
