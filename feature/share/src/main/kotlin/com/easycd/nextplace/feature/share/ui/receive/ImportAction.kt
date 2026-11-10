package com.easycd.nextplace.feature.share.ui.receive

sealed interface ImportAction {
    data class Toggle(val index: Int) : ImportAction
    data object Add : ImportAction
    data object Close : ImportAction
}
