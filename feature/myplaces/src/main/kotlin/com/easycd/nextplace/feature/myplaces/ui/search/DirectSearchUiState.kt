package com.easycd.nextplace.feature.myplaces.ui.search

import com.easycd.nextplace.core.domain.model.PoiCandidate

/** S-09b 직접 검색. */
data class DirectSearchUiState(
    val query: String = "",
    val results: List<PoiCandidate> = emptyList(),
    val selected: Int? = null,
    val loading: Boolean = false,
    val offline: Boolean = false,
)
