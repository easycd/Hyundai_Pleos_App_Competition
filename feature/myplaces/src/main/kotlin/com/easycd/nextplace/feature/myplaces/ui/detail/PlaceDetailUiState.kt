package com.easycd.nextplace.feature.myplaces.ui.detail

import com.easycd.nextplace.core.domain.model.GeoPoint

/** S-08 가게 상세 (+ S-08c 삭제 확인 창). */
data class PlaceDetailUiState(
    val loaded: Boolean = false,
    val id: String = "",
    val name: String = "",
    val category: String? = null,
    val distanceM: Double? = null,
    val address: String? = null,
    val savedAt: Long = 0,
    val visits: Int = 0,
    val memo: String? = null,
    val visitNotes: List<String> = emptyList(),
    val utterance: String? = null,
    val point: GeoPoint? = null,
    val remindEnabled: Boolean = true,
    val globalRemind: Boolean = true,
    val inShareCart: Boolean = false,
    val confirmDelete: Boolean = false,
)
