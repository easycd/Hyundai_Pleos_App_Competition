package com.easycd.nextplace.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** 애니메이션 기준 (화면 설계서 5장). */
object Motion {
    val standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val emphasized = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    const val FAST_MS = 120
    const val SCRIM_MS = 160
    const val BASE_MS = 200
    const val ALERT_MS = 240
    const val PIN_DROP_MS = 400
    val driveShift = 24.dp
    val parkedShift = 48.dp
}

/** 시스템 애니메이션 배율이 0이면 true (전환은 페이드만, 반복은 소리 막대만). */
val LocalReduceMotion = staticCompositionLocalOf { false }

object ReduceMotion {
    val enabled: Boolean
        @Composable get() = LocalReduceMotion.current
}
