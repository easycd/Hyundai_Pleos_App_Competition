package com.easycd.nextplace.feature.myplaces.ui.search

import com.easycd.nextplace.core.domain.model.PoiCandidate

/** S-09b 직접 검색. */
data class DirectSearchUiState(
    val query: String = "",
    val results: List<PoiCandidate> = emptyList(),
    val selected: Int? = null,
    val loading: Boolean = false,
    val offline: Boolean = false,
    /** [results] 가 어떤 검색어의 결과인가. 지금 입력과 다르면(새로 입력 중) 저장할 수 없다. */
    val resultsFor: String = "",
) {
    val canSave: Boolean get() = selected != null && !loading && resultsFor == query.trim()
}
