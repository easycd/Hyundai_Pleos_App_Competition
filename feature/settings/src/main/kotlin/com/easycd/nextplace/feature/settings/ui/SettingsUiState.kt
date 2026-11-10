package com.easycd.nextplace.feature.settings.ui

import com.easycd.nextplace.core.domain.model.AppSettings

/** S-11 설정 (+ S-11b 모두 삭제 확인). */
data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val total: Int = 0,
    val confirmDeleteAll: Boolean = false,
)
