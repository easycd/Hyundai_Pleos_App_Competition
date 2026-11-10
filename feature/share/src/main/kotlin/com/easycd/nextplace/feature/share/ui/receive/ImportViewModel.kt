package com.easycd.nextplace.feature.share.ui.receive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Messages
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.contract.RecentImport
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.ui.nav.Routes
import com.easycd.nextplace.feature.share.codec.DecodeResult
import com.easycd.nextplace.feature.share.codec.ShareCodec
import com.easycd.nextplace.feature.share.usecase.ImportPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val importer: ImportPlacesUseCase,
    private val recent: RecentImport,
    private val navigator: Navigator,
    private val messages: Messages,
) : ViewModel() {
    private val payload: String = handle[Routes.ARG_PAYLOAD] ?: ""
    private val _state = MutableStateFlow(ImportUiState())
    val state: StateFlow<ImportUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = when (val r = ShareCodec.decode(payload)) {
                DecodeResult.Broken -> ImportUiState(loaded = true, problem = ImportProblem.BROKEN)
                DecodeResult.TooNew -> ImportUiState(loaded = true, problem = ImportProblem.TOO_NEW)
                is DecodeResult.Ok -> ImportUiState(
                    loaded = true,
                    title = r.payload.title,
                    createdAt = r.payload.createdAt,
                    from = r.payload.from,
                    rows = r.payload.places.map { p -> importer.exists(p).let { ImportRow(p, it, !it) } },
                )
            }
        }
    }

    fun onAction(a: ImportAction) {
        when (a) {
            is ImportAction.Toggle -> _state.update { s ->
                s.copy(rows = s.rows.mapIndexed { i, r -> if (i == a.index && !r.exists) r.copy(checked = !r.checked) else r })
            }
            ImportAction.Close -> navigator.go(Destination.Back)
            ImportAction.Add -> add()
        }
    }

    private fun add() {
        val chosen = _state.value.rows.filter { it.checked && !it.exists }.map { it.place }
        if (chosen.isEmpty()) return
        _state.update { it.copy(adding = true) }
        viewModelScope.launch {
            importer.import(chosen)
                .onSuccess { ids ->
                    recent.set(ids.toSet())
                    messages.show(UserMessage.Imported(ids.size))
                    navigator.go(Destination.MyPlaces)
                }
                .onFailure {
                    _state.update { s -> s.copy(adding = false) }
                    messages.show(UserMessage.ImportFailed)
                }
        }
    }
}
