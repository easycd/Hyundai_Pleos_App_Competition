package com.easycd.nextplace.feature.myplaces.ui.search

sealed interface DirectSearchAction {
    data class Query(val text: String) : DirectSearchAction
    data class Pick(val index: Int) : DirectSearchAction
    data object Save : DirectSearchAction
    data object Back : DirectSearchAction
}
