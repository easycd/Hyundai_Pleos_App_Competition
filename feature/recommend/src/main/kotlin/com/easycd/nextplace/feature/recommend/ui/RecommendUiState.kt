package com.easycd.nextplace.feature.recommend.ui

import com.easycd.nextplace.feature.recommend.session.RecommendState

/** S-06 · S-06b · S-06c. 세션 상태를 그대로 그린다. */
data class RecommendUiState(val session: RecommendState = RecommendState.Loading)
