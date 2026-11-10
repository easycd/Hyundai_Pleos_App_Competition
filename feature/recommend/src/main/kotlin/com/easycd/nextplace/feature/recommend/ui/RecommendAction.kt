package com.easycd.nextplace.feature.recommend.ui

sealed interface RecommendAction {
    data class Select(val index: Int) : RecommendAction
    data object More : RecommendAction
    data object Widen : RecommendAction
    data object Back : RecommendAction
}
