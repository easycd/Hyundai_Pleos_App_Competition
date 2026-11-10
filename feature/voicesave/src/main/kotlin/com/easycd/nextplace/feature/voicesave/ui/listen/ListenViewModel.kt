package com.easycd.nextplace.feature.voicesave.ui.listen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.core.domain.sdk.DevVoiceInput
import com.easycd.nextplace.feature.voicesave.pipeline.SaveFlowState
import com.easycd.nextplace.feature.voicesave.pipeline.SavePipeline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ListenViewModel @Inject constructor(
    private val pipeline: SavePipeline,
    private val dev: DevVoiceInput,
) : ViewModel() {
    /** 개발용 입력 칸 글자. 듣기가 한 번 끝나 다시 들을 때도 지워지지 않게 화면 밖에 둔다. */
    private val draft = MutableStateFlow("")

    val state: StateFlow<ListenUiState> = combine(pipeline.state, draft) { s, typed ->
        when (s) {
            is SaveFlowState.Listening -> ListenUiState(ListenPhase.LISTENING, s.partial, s.level, devInput = dev.enabled, devDraft = typed)
            is SaveFlowState.Understanding -> ListenUiState(ListenPhase.UNDERSTANDING, s.text)
            SaveFlowState.Retry -> ListenUiState(ListenPhase.RETRY)
            is SaveFlowState.Searching -> ListenUiState(ListenPhase.SEARCHING, s.text, target = s.target, current = s.current)
            else -> ListenUiState()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ListenUiState())

    fun onAction(a: ListenAction) {
        when (a) {
            ListenAction.Cancel -> pipeline.cancel()
            is ListenAction.DevDraft -> draft.value = a.text
            is ListenAction.DevSubmit -> if (a.text.isNotBlank()) {
                dev.submit(a.text)
                draft.value = ""
            }
        }
    }
}
