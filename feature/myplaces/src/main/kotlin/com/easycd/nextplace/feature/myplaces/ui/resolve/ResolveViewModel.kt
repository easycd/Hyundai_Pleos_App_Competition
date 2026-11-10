package com.easycd.nextplace.feature.myplaces.ui.resolve

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Messages
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.domain.poi.PoiQuery
import com.easycd.nextplace.core.domain.poi.PoiResult
import com.easycd.nextplace.core.domain.poi.PoiSource
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.ui.nav.Routes
import com.easycd.nextplace.core.ui.text.HeadingText
import com.easycd.nextplace.feature.myplaces.usecase.DeletePlaceUseCase
import com.easycd.nextplace.feature.myplaces.usecase.ResolvePlaceUseCase
import com.easycd.nextplace.feature.myplaces.usecase.ResolveResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * S-09: 그때 상황(저장 시각·위치·진행 방향·말한 문장)과 후보 5곳(점수순, 기본 선택 1순위).
 * 후보가 없고 위치가 있으면(오프라인 저장) 지금 다시 찾아 본다.
 */
@HiltViewModel
class ResolveViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val places: PlaceRepository,
    private val poi: PoiSource,
    private val resolve: ResolvePlaceUseCase,
    private val delete: DeletePlaceUseCase,
    private val navigator: Navigator,
    private val messages: Messages,
) : ViewModel() {
    private val id: String = checkNotNull(handle[Routes.ARG_ID])
    private var place: Place? = null
    private val _state = MutableStateFlow(ResolveUiState())
    val state: StateFlow<ResolveUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val p = places.get(id) ?: return
        place = p
        val point = p.point
        _state.value = ResolveUiState(
            loaded = true,
            savedAt = p.savedAt,
            where = p.address,
            heading = p.savedHeading?.let(HeadingText::of),
            utterance = p.utterance,
            noLocation = point == null,
            estimated = point,
            candidates = p.candidates.take(Tunables.KEEP_CANDIDATES),
            title = p.displayName ?: "",
        )
        if (p.candidates.isEmpty() && point != null) research(point)
    }

    private suspend fun research(point: GeoPoint) {
        _state.update { it.copy(searching = true) }
        val r = poi.search(PoiQuery(point, Tunables.POI_RADIUS_M))
        val found = (r as? PoiResult.Found)?.candidates.orEmpty()
            .map { it.copy(distanceM = Geo.distanceM(point, GeoPoint(it.lat, it.lon))) }
            .take(Tunables.KEEP_CANDIDATES)
        _state.update { it.copy(searching = false, candidates = found, selected = 0) }
    }

    fun onAction(a: ResolveAction) {
        when (a) {
            is ResolveAction.Pick -> _state.update { it.copy(selected = a.index) }
            ResolveAction.Save -> save()
            ResolveAction.Search -> navigator.go(Destination.Search(id))
            ResolveAction.Delete -> _state.update { it.copy(confirmDelete = true) }
            ResolveAction.CancelDelete -> _state.update { it.copy(confirmDelete = false) }
            ResolveAction.ConfirmDelete -> {
                _state.update { it.copy(confirmDelete = false) }
                delete.delete(id, place?.displayName ?: "")
                navigator.go(Destination.Back)
            }
            ResolveAction.Back -> navigator.go(Destination.Back)
        }
    }

    private fun save() {
        val p = place ?: return
        val c = _state.value.candidates.getOrNull(_state.value.selected) ?: return
        viewModelScope.launch {
            when (val r = resolve(p, c)) {
                is ResolveResult.Saved -> {
                    messages.show(UserMessage.ResolvedSaved)
                    navigator.go(Destination.PlaceDetail(r.id))
                }
                is ResolveResult.Merged -> {
                    messages.show(UserMessage.Merged)
                    navigator.go(Destination.PlaceDetail(r.id))
                }
                ResolveResult.Failed -> messages.show(UserMessage.SaveFailed)
            }
        }
    }
}
