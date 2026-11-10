package com.easycd.nextplace.feature.myplaces.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Messages
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.domain.poi.PoiQuery
import com.easycd.nextplace.core.domain.poi.PoiResult
import com.easycd.nextplace.core.domain.poi.PoiSource
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.sdk.LocationSource
import com.easycd.nextplace.core.ui.nav.Routes
import com.easycd.nextplace.feature.myplaces.usecase.ResolvePlaceUseCase
import com.easycd.nextplace.feature.myplaces.usecase.ResolveResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 이름 일부로 그때 위치 2km 안을 찾는다. 입력이 0.3초 멈추면 검색, 늦게 온 이전 결과는 버린다(collectLatest). */
@OptIn(FlowPreview::class)
@HiltViewModel
class DirectSearchViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val places: PlaceRepository,
    private val poi: PoiSource,
    private val location: LocationSource,
    private val resolve: ResolvePlaceUseCase,
    private val navigator: Navigator,
    private val messages: Messages,
) : ViewModel() {
    private val id: String = checkNotNull(handle[Routes.ARG_ID])
    private val _state = MutableStateFlow(DirectSearchUiState())
    val state: StateFlow<DirectSearchUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.map { it.query.trim() }.distinctUntilChanged().debounce(Tunables.SEARCH_DEBOUNCE_MS).collectLatest { q -> search(q) }
        }
    }

    private suspend fun search(q: String) {
        if (q.isEmpty()) return _state.update { it.copy(results = emptyList(), selected = null, loading = false, offline = false) }
        val center = places.get(id)?.point ?: location.latest.value?.point ?: return
        _state.update { it.copy(loading = true) }
        val r = poi.search(PoiQuery(center, Tunables.SEARCH_RADIUS_M, keyword = q))
        val list = (r as? PoiResult.Found)?.candidates.orEmpty()
            .map { it.copy(distanceM = Geo.distanceM(center, GeoPoint(it.lat, it.lon))) }
        _state.update { it.copy(results = list, selected = if (list.isEmpty()) null else 0, loading = false, offline = r is PoiResult.Offline) }
    }

    fun onAction(a: DirectSearchAction) {
        when (a) {
            is DirectSearchAction.Query -> _state.update { it.copy(query = a.text) }
            is DirectSearchAction.Pick -> _state.update { it.copy(selected = a.index) }
            DirectSearchAction.Back -> navigator.go(Destination.Back)
            DirectSearchAction.Save -> save()
        }
    }

    private fun save() {
        val s = _state.value
        val c = s.selected?.let { s.results.getOrNull(it) } ?: return
        viewModelScope.launch {
            val p = places.get(id) ?: return@launch
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
