package com.easycd.nextplace.feature.remind.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.feature.remind.pipeline.RemindPipeline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RemindViewModel @Inject constructor(private val pipeline: RemindPipeline) : ViewModel() {
    val state: StateFlow<RemindUiState> = pipeline.state.map { RemindUiState(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RemindUiState())

    fun onAction(a: RemindAction) = pipeline.press(a == RemindAction.Go)
}
