package com.easycd.nextplace.feature.settings.ui

import com.easycd.nextplace.core.domain.model.AppSettings

/** S-11 설정 (+ S-11b 모두 삭제 확인). */
data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val total: Int = 0,
    val confirmDeleteAll: Boolean = false,
    /** false: 음성 SDK 에 음량 조절이 없음 (실제 Pleos). 음량 대신 켜기/끄기만 보인다 (음량은 차량 음량을 따름). */
    val volumeAdjustable: Boolean = true,
)
