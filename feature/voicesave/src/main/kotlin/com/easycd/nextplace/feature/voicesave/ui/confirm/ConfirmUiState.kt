package com.easycd.nextplace.feature.voicesave.ui.confirm

import com.easycd.nextplace.core.domain.model.Side

/** S-03 확인 / S-03b 두 번째 후보. [waitStartedAt] = TTS 가 끝난 시각 (카운트다운·듣는 중 표시 시작). */
data class ConfirmUiState(
    val visible: Boolean = false,
    val key: String = "",
    val second: Boolean = false,
    val name: String = "",
    val category: String? = null,
    val distanceM: Int = 0,
    val ahead: Boolean = false,
    val side: Side = Side.UNKNOWN,
    val address: String? = null,
    val waitStartedAt: Long? = null,
)
