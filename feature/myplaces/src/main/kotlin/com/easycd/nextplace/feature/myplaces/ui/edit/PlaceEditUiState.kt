package com.easycd.nextplace.feature.myplaces.ui.edit

/** S-08b 가게 수정. */
data class PlaceEditUiState(
    val loaded: Boolean = false,
    val name: String = "",
    val category: String? = null,
    val memo: String = "",
    val categories: List<String> = emptyList(),
    val canRepick: Boolean = true,
) {
    val canSave: Boolean get() = name.isNotBlank()
}
