package com.easycd.nextplace.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.ui.theme.Np
import kotlin.math.PI
import kotlin.math.sin

/** 소리 막대 (S-02a: 17개, 900ms 반복, 막대마다 60ms 어긋남, 마이크 크기에 비례). */
@Composable
fun SoundBars(level: Float, modifier: Modifier = Modifier, count: Int = 17, color: Color = Np.colors.brand, active: Boolean = true) {
    val t = rememberInfiniteTransition(label = "bars")
    val phase by t.animateFloat(0f, 1f, infiniteRepeatable(tween(BAR_PERIOD_MS, easing = LinearEasing)), label = "phase")
    Canvas(modifier) {
        val gap = size.width / (count * 2f)
        val w = gap
        for (i in 0 until count) {
            val wave = if (active) (sin(2 * PI * (phase - i * BAR_OFFSET)).toFloat() + 1f) / 2f else 0f
            val amp = MIN_BAR + (1f - MIN_BAR) * wave * (0.35f + 0.65f * level.coerceIn(0f, 1f))
            val h = size.height * if (active) amp else MIN_BAR
            drawRoundRect(color, Offset(i * 2 * gap + gap / 2, (size.height - h) / 2), Size(w, h), CornerRadius(w / 2))
        }
    }
}

/** 처리 링 72dp (S-02b · S-14: 1초 한 바퀴). */
@Composable
fun SpinnerRing(modifier: Modifier = Modifier, size: Dp = 72.dp) {
    val t = rememberInfiniteTransition(label = "ring")
    val angle by t.animateFloat(0f, FULL, infiniteRepeatable(tween(1_000, easing = LinearEasing)), label = "angle")
    val c = Np.colors
    Canvas(modifier.size(size)) {
        val stroke = Stroke(width = size.toPx() / 10f, cap = StrokeCap.Round)
        drawArc(c.glassBorder, 0f, FULL, false, style = stroke)
        drawArc(c.brand, angle, ARC, false, style = stroke)
    }
}

/** 확인 대기 링 (S-03: 5초 동안 테두리가 줄어듦). [fraction] 1 → 0. */
@Composable
fun CountdownRing(fraction: Float, modifier: Modifier = Modifier, size: Dp = 56.dp) {
    val c = Np.colors
    Canvas(modifier.size(size)) {
        val stroke = Stroke(width = size.toPx() / 9f, cap = StrokeCap.Round)
        drawArc(c.glassBorder, 0f, FULL, false, style = stroke)
        drawArc(c.brandText, -QUARTER, FULL * fraction.coerceIn(0f, 1f), false, style = stroke)
    }
}

private const val BAR_PERIOD_MS = 900
private const val BAR_OFFSET = 0.0667f
private const val MIN_BAR = 0.3f
private const val FULL = 360f
private const val ARC = 100f
private const val QUARTER = 90f
