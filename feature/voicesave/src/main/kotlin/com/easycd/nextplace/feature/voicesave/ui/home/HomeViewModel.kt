package com.easycd.nextplace.feature.voicesave.ui.home

import kotlinx.coroutines.flow.collectLatest
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.GeoPoint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.contract.PermissionKind
import com.easycd.nextplace.core.domain.contract.RecommendRunner
import com.easycd.nextplace.core.domain.contract.SaveRunner
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.model.PlaceWithVisits
import com.easycd.nextplace.core.domain.poi.ReverseGeocoder
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.sdk.LocationSource
import com.easycd.nextplace.core.domain.sdk.NaviController
import com.easycd.nextplace.core.ui.text.HeadingText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val places: PlaceRepository,
    private val location: LocationSource,
    private val navi: NaviController,
    private val geocoder: ReverseGeocoder,
    private val navigator: Navigator,
    private val saveRunner: SaveRunner,
    private val recommend: RecommendRunner,
) : ViewModel() {
    private val geocoded = MutableStateFlow<String?>(null)
    private val nearby = MutableStateFlow(0)

    val state: StateFlow<HomeUiState> = combine(
        places.observeCounts(),
        places.observeLatest(),
        location.latest,
        navi.roadName,
        combine(geocoded, nearby) { g, n -> g to n },
    ) { counts, latest, sample, naviRoad, (geo, near) ->
        HomeUiState(
            locationLabel = if (!location.hasPermission()) null else LocationLabel(naviRoad ?: geo, sample?.heading?.let { HeadingText.of(it.toDouble()) }),
            total = counts.total,
            unconfirmed = counts.unconfirmed,
            nearbyCount = near,
            latest = latest?.let(::card),
            simulated = location.isSimulated,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_MS), HomeUiState())

    init {
        viewModelScope.launch { refreshRoadName() }
        // '근처 저장 가게 N곳': 가게가 늘거나 줄거나 확인될 때, 첫 위치가 잡힐 때, 마지막 계산 뒤 300m 넘게 움직였을 때 다시 센다
        viewModelScope.launch {
            var lastCounts: Any? = null
            var lastAt: GeoPoint? = null
            combine(places.observeCounts(), location.latest.map { it?.point }) { c, p -> c to p }.collectLatest { (c, p) ->
                val moved = p != null && (lastAt == null || Geo.distanceM(lastAt!!, p) > Tunables.HOME_NEARBY_REFRESH_M)
                if (c != lastCounts || moved) {
                    lastCounts = c
                    if (p != null) lastAt = p
                    refreshNearby()
                }
            }
        }
    }

    fun onAction(a: HomeAction) {
        when (a) {
            HomeAction.Speak -> saveRunner.startVoiceSave()
            HomeAction.Recommend -> openRecommend()
            HomeAction.MyPlaces -> navigator.go(Destination.MyPlaces)
            HomeAction.Settings -> navigator.go(Destination.Settings)
            HomeAction.OpenLatest -> state.value.latest?.let {
                navigator.go(if (it.needsResolve) Destination.Resolve(it.id) else Destination.PlaceDetail(it.id))
            }
        }
    }

    private fun openRecommend() {
        if (!location.hasPermission()) return navigator.go(Destination.PermissionNeeded(PermissionKind.LOCATION))
        recommend.open(null, location.capture(PlaceSource.VOICE))
    }

    /** 앱이 앞에 있을 때 30초에 1회 좌표 → 도로명 (내비가 도로 이름을 주면 그것을 쓴다). */
    private suspend fun refreshRoadName() {
        while (viewModelScope.isActive) {
            if (navi.roadName.value == null) {
                location.latest.value?.let { geocoded.value = geocoder.roadName(it.point) ?: geocoded.value }
            }
            delay(Tunables.ROAD_NAME_REFRESH_MS)
        }
    }

    private suspend fun refreshNearby() {
        val here = location.latest.value?.point
        nearby.value = if (here == null) 0 else {
            places.nearbyConfirmed(here, Tunables.RECOMMEND_RADIUS_M.toDouble()).size.coerceAtMost(Tunables.RECOMMEND_PAGE_SIZE)
        }
    }

    private fun card(p: PlaceWithVisits): LatestCard {
        val place = p.place
        val unconfirmed = place.status == PlaceStatus.UNCONFIRMED
        val chip = when {
            unconfirmed && place.candidatesFoundLater -> LatestChip.FOUND
            unconfirmed -> LatestChip.UNCONFIRMED
            p.visitCount > 0 -> LatestChip.VISITED
            else -> LatestChip.SAVED
        }
        return LatestCard(
            id = place.id,
            name = place.displayName,
            detail = if (unconfirmed) place.address else null,
            noLocation = place.point == null,
            savedAt = place.savedAt,
            chip = chip,
            visits = p.visitCount,
            needsResolve = unconfirmed,
        )
    }

    private companion object {
        const val STOP_MS = 5_000L
    }
}
