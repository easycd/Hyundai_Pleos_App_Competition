package com.easycd.nextplace.feature.myplaces.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.contract.RecentImport
import com.easycd.nextplace.core.domain.contract.SaveRunner
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.sdk.LocationSource
import com.easycd.nextplace.feature.myplaces.ui.common.toPin
import com.easycd.nextplace.feature.myplaces.ui.common.toRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class ListOptions(val filter: PlaceFilter = PlaceFilter.ALL, val sort: PlaceSort = PlaceSort.NEAR, val selected: String? = null)

@HiltViewModel
class MyPlacesViewModel @Inject constructor(
    places: PlaceRepository,
    private val location: LocationSource,
    private val recent: RecentImport,
    private val navigator: Navigator,
    private val saveRunner: SaveRunner,
) : ViewModel() {
    private val options = MutableStateFlow(ListOptions())

    val state: StateFlow<MyPlacesUiState> = combine(places.observeAll(), location.latest, options, recent.ids) { all, here, o, fresh ->
        val me = here?.point
        val filtered = all.filter {
            when (o.filter) {
                PlaceFilter.ALL -> true
                PlaceFilter.UNCONFIRMED -> it.place.status == PlaceStatus.UNCONFIRMED
                PlaceFilter.VISITED -> it.visitCount > 0
            }
        }
        val withDist = filtered.map { p -> p to me?.let { m -> p.place.point?.let { Geo.distanceM(m, it) } } }
        // 가져온 직후(S-16b)에는 새 줄이 위에 오도록 최근 저장 순. 위치를 모르면 가까운 순을 쓸 수 없다.
        val sort = if (fresh.isNotEmpty() || me == null) PlaceSort.RECENT else o.sort
        val sorted = when (sort) {
            PlaceSort.NEAR -> withDist.sortedBy { it.second ?: Double.MAX_VALUE }
            PlaceSort.RECENT -> withDist.sortedByDescending { it.first.place.savedAt }
        }
        MyPlacesUiState(
            loaded = true,
            total = all.size,
            unconfirmedCount = all.count { it.place.status == PlaceStatus.UNCONFIRMED },
            visitedCount = all.count { it.visitCount > 0 },
            rows = sorted.map { (p, d) -> p.toRow(d) },
            pins = filtered.mapNotNull { it.toPin(it.place.id == o.selected) },
            me = me,
            filter = o.filter,
            sort = sort,
            selectedId = o.selected,
            freshIds = fresh,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_MS), MyPlacesUiState())

    init {
        viewModelScope.launch {
            if (recent.ids.value.isNotEmpty()) {
                delay(FRESH_KEEP_MS)
                recent.set(emptySet())
            }
        }
    }

    fun onAction(a: MyPlacesAction) {
        when (a) {
            is MyPlacesAction.Filter -> options.update { it.copy(filter = a.filter) }
            is MyPlacesAction.Sort -> options.update { it.copy(sort = a.sort) }
            is MyPlacesAction.Open -> open(a.id)
            is MyPlacesAction.Pin -> {
                val row = state.value.rows.firstOrNull { it.id == a.id }
                if (row?.unconfirmed == true) open(a.id) else options.update { it.copy(selected = a.id) }
            }
            MyPlacesAction.Share -> navigator.go(Destination.Share())
            MyPlacesAction.Speak -> saveRunner.startVoiceSave()
            MyPlacesAction.Back -> navigator.go(Destination.Back)
        }
    }

    private fun open(id: String) {
        options.update { it.copy(selected = id) }
        val row = state.value.rows.firstOrNull { it.id == id } ?: return
        navigator.go(if (row.unconfirmed) Destination.Resolve(id) else Destination.PlaceDetail(id))
    }

    private companion object {
        const val STOP_MS = 5_000L
        const val FRESH_KEEP_MS = 1_500L
    }
}
