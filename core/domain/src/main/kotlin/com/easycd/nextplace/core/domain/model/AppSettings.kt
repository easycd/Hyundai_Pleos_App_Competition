package com.easycd.nextplace.core.domain.model

import com.easycd.nextplace.core.domain.Tunables

/** 설정 (기술서 8장 DataStore 키 7개, S-11). */
data class AppSettings(
    val remindEnabled: Boolean = true,
    val remindRadiusM: Int = Tunables.REMIND_RADIUS_DEFAULT_M,
    val mealTimeOnly: Boolean = true,
    val voiceVolume: Float = DEFAULT_VOLUME,
    val lookbackSec: Int = Tunables.LOOKBACK_DEFAULT_SEC,
    val lastRemindAt: Long? = null,
    val onboardingDone: Boolean = false,
) {
    companion object {
        const val DEFAULT_VOLUME = 0.8f
    }
}
