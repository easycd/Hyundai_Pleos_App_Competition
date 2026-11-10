package com.easycd.nextplace.feature.myplaces.ui.resolve

sealed interface ResolveAction {
    data class Pick(val index: Int) : ResolveAction
    data object Save : ResolveAction
    data object Search : ResolveAction
    data object Delete : ResolveAction
    data object ConfirmDelete : ResolveAction
    data object CancelDelete : ResolveAction
    data object Back : ResolveAction
}
