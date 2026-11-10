package com.easycd.nextplace.feature.myplaces.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Messages
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.usecase.withDbRetry
import com.easycd.nextplace.core.domain.voice.Categories
import com.easycd.nextplace.core.ui.nav.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaceEditViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val places: PlaceRepository,
    private val navigator: Navigator,
    private val messages: Messages,
) : ViewModel() {
    private val id: String = checkNotNull(handle[Routes.ARG_ID])
    private val _state = MutableStateFlow(PlaceEditUiState(categories = Categories.all.map { it.name }))
    val state: StateFlow<PlaceEditUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val pw = places.getWithVisits(id) ?: return@launch
            val p = pw.place
            _state.update {
                it.copy(loaded = true, name = p.name.orEmpty(), category = p.category, memo = p.memo.orEmpty(), canRepick = pw.visitCount == 0)
            }
        }
    }

    fun onAction(a: PlaceEditAction) {
        when (a) {
            is PlaceEditAction.Name -> _state.update { it.copy(name = a.value.take(Tunables.SHARE_TEXT_MAX)) }
            is PlaceEditAction.Category -> _state.update { it.copy(category = a.value) }
            is PlaceEditAction.Memo -> _state.update { it.copy(memo = a.value.take(MEMO_MAX)) }
            PlaceEditAction.Repick -> if (_state.value.canRepick) navigator.go(Destination.Resolve(id))
            PlaceEditAction.Cancel -> navigator.go(Destination.Back)
            PlaceEditAction.Save -> save()
        }
    }

    private fun save() {
        val s = _state.value
        if (!s.canSave) return
        viewModelScope.launch {
            val ok = withDbRetry {
                val p = places.get(id) ?: return@withDbRetry
                places.update(p.copy(name = s.name.trim(), category = s.category, memo = s.memo.trim().ifBlank { null }))
            }.isSuccess
            messages.show(if (ok) UserMessage.Edited else UserMessage.SaveFailed)
            if (ok) navigator.go(Destination.Back)
        }
    }

    private companion object {
        const val MEMO_MAX = 100
    }
}
