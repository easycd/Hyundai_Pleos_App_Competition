package com.easycd.nextplace.feature.voicesave.ui.listen

sealed interface ListenAction {
    data object Cancel : ListenAction
    data class DevDraft(val text: String) : ListenAction
    data class DevSubmit(val text: String) : ListenAction
}
