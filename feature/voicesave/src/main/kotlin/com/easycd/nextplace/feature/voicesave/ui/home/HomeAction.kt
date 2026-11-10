package com.easycd.nextplace.feature.voicesave.ui.home

sealed interface HomeAction {
    data object Speak : HomeAction
    data object Recommend : HomeAction
    data object MyPlaces : HomeAction
    data object OpenLatest : HomeAction
    data object Settings : HomeAction
}
