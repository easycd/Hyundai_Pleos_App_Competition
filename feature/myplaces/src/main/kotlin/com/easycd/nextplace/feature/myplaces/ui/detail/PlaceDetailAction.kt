package com.easycd.nextplace.feature.myplaces.ui.detail

sealed interface PlaceDetailAction {
    data object Route : PlaceDetailAction
    data object Edit : PlaceDetailAction
    data object Delete : PlaceDetailAction
    data object ConfirmDelete : PlaceDetailAction
    data object CancelDelete : PlaceDetailAction
    data object AddToShare : PlaceDetailAction
    data class Remind(val on: Boolean) : PlaceDetailAction
    data object Back : PlaceDetailAction
}
