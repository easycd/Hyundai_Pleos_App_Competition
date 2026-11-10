package com.easycd.nextplace.feature.recommend.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.feature.recommend.session.RecommendSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RecommendViewModel @Inject constructor(private val session: RecommendSession) : ViewModel() {
    val state: StateFlow<RecommendUiState> = session.state.map { RecommendUiState(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RecommendUiState(session.state.value))

    fun onAction(a: RecommendAction) {
        when (a) {
            is RecommendAction.Select -> session.select(a.index)
            RecommendAction.More -> session.next()
            RecommendAction.Widen -> session.widen()
            RecommendAction.Back -> session.back()
        }
    }
}
