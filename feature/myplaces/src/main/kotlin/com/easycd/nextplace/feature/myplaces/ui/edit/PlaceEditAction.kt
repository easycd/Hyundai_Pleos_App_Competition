package com.easycd.nextplace.feature.myplaces.ui.edit

sealed interface PlaceEditAction {
    data class Name(val value: String) : PlaceEditAction
    data class Category(val value: String) : PlaceEditAction
    data class Memo(val value: String) : PlaceEditAction
    data object Repick : PlaceEditAction
    data object Save : PlaceEditAction
    data object Cancel : PlaceEditAction
}
