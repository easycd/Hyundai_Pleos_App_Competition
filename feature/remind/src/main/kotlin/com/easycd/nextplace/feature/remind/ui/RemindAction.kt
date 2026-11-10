package com.easycd.nextplace.feature.remind.ui

sealed interface RemindAction {
    data object Go : RemindAction
    data object Close : RemindAction
}
