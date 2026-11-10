package com.easycd.nextplace.feature.share.ui.send

sealed interface ShareAction {
    data class Toggle(val id: String) : ShareAction
    data object ToggleAll : ShareAction
    data class Title(val text: String) : ShareAction
    data object Copy : ShareAction
    data object Send : ShareAction
    data object Back : ShareAction
}
