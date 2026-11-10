package com.easycd.nextplace.feature.voicesave.ui.confirm

sealed interface ConfirmAction {
    data object Yes : ConfirmAction
    data object No : ConfirmAction
}
