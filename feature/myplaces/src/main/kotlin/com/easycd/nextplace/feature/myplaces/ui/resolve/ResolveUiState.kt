package com.easycd.nextplace.feature.myplaces.ui.resolve

import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.PoiCandidate

/** S-09 미확인 가게 정리 (+ S-08c 삭제 확인). */
data class ResolveUiState(
    val loaded: Boolean = false,
    val savedAt: Long = 0,
    val where: String? = null,
    val heading: String? = null,
    val utterance: String? = null,
    val noLocation: Boolean = false,
    val estimated: GeoPoint? = null,
    val candidates: List<PoiCandidate> = emptyList(),
    val selected: Int = 0,
    val searching: Boolean = false,
    val confirmDelete: Boolean = false,
    val title: String = "",
)
