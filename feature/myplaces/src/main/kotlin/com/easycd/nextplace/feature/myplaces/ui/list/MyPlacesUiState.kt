package com.easycd.nextplace.feature.myplaces.ui.list

import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.ui.component.MapPin
import com.easycd.nextplace.feature.myplaces.ui.common.PlaceRowModel

enum class PlaceFilter { ALL, UNCONFIRMED, VISITED }
enum class PlaceSort { NEAR, RECENT }

/** S-07 내 맛집 · S-07b 저장 전 (total == 0). */
data class MyPlacesUiState(
    val loaded: Boolean = false,
    val total: Int = 0,
    val rows: List<PlaceRowModel> = emptyList(),
    val pins: List<MapPin> = emptyList(),
    val me: GeoPoint? = null,
    val filter: PlaceFilter = PlaceFilter.ALL,
    val sort: PlaceSort = PlaceSort.RECENT,
    val selectedId: String? = null,
    val freshIds: Set<String> = emptySet(),
)
