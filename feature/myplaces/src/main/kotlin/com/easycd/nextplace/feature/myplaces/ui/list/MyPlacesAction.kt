package com.easycd.nextplace.feature.myplaces.ui.list

sealed interface MyPlacesAction {
    data class Filter(val filter: PlaceFilter) : MyPlacesAction
    data class Sort(val sort: PlaceSort) : MyPlacesAction
    data class Open(val id: String) : MyPlacesAction
    data class Pin(val id: String) : MyPlacesAction
    data object Share : MyPlacesAction
    data object Speak : MyPlacesAction
    data object Back : MyPlacesAction
}
