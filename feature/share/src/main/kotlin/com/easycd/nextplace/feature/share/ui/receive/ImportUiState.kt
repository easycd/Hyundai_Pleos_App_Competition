package com.easycd.nextplace.feature.share.ui.receive

import com.easycd.nextplace.core.domain.model.SharedPlace

enum class ImportProblem { NONE, BROKEN, TOO_NEW }

data class ImportRow(val place: SharedPlace, val exists: Boolean, val checked: Boolean)

/** S-16 받은 목록 가져오기. */
data class ImportUiState(
    val loaded: Boolean = false,
    val problem: ImportProblem = ImportProblem.NONE,
    val title: String = "",
    val createdAt: Long = 0,
    val from: String? = null,
    val rows: List<ImportRow> = emptyList(),
    val adding: Boolean = false,
) {
    val canAdd: Boolean get() = rows.any { it.checked && !it.exists } && !adding
    val toAdd: Int get() = rows.count { it.checked && !it.exists }
    val existing: Int get() = rows.count { it.exists }
}
