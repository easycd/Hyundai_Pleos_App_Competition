package com.easycd.nextplace.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 앱 아이콘 (확정 15번: 주황 배경, 흰 핀, 주황 원 배지, 숟가락·젓가락 X, 젓가락이 앞).
 * docs/design/icons/final/app_icon.svg 를 100 격자 그대로 그린다. S-00 시작 애니메이션은
 * [spoon]·[sticks] (0 = 원 아래 숨음, 1 = 제자리)와 [badge] 크기로 움직인다.
 */
@Composable
fun AppMark(modifier: Modifier = Modifier, size: Dp = 56.dp, spoon: Float = 1f, sticks: Float = 1f, badge: Float = 1f) {
    Canvas(modifier.size(size)) {
        val k = this.size.minDimension / GRID
        scale(k, k, pivot = Offset.Zero) { drawMark(spoon, sticks, badge) }
    }
}

private val pinPath by lazy { PathParser().parsePathString("M50 90C50 90 20 60 20 41a30 30 0 1 1 60 0C80 60 50 90 50 90Z").toPath() }
private val spoonPath by lazy {
    PathParser().parsePathString(
        "M0 -31C8.5 -31 12.5 -22 12.5 -13C12.5 -6.5 9.5 -2 6 0C4.4 0.9 3.3 1.8 3.2 3.4L3.8 27.5A3.8 3.8 0 0 1 -3.8 27.5L-3.2 3.4C-3.3 1.8 -4.4 0.9 -6 0C-9.5 -2 -12.5 -6.5 -12.5 -13C-12.5 -22 -8.5 -31 0 -31Z",
    ).toPath()
}

private fun DrawScope.drawMark(spoon: Float, sticks: Float, badge: Float) {
    val orange = Color(0xFFE4572E)
    drawRoundRect(orange, Offset.Zero, Size(GRID, GRID), CornerRadius(RADIUS))
    drawPath(pinPath, Color.White)
    drawPath(pinPath, Color.White, style = Stroke(width = 4f))
    val center = Offset(CX, CY)
    scale(badge, badge, pivot = center) {
        drawCircle(orange, BADGE_R, center)
        val clip = Path().apply { addOval(androidx.compose.ui.geometry.Rect(center, BADGE_R)) }
        clipPath(clip) {
            translate(CX, CY) {
                scale(UTENSIL_SCALE, UTENSIL_SCALE, pivot = Offset.Zero) {
                    rotate(SPOON_DEG, pivot = Offset.Zero) {
                        translate(0f, (1f - spoon) * TRAVEL) { drawPath(spoonPath, Color.White) }
                    }
                    rotate(-SPOON_DEG, pivot = Offset.Zero) {
                        translate(0f, (1f - sticks) * TRAVEL) { drawSticks(orange) }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawSticks(orange: Color) {
    listOf(-STICK_X, STICK_X).forEach { x ->
        drawLine(orange, Offset(x, -STICK_LEN), Offset(x, STICK_LEN), OUTLINE, StrokeCap.Round)
    }
    listOf(-STICK_X, STICK_X).forEach { x ->
        drawLine(Color.White, Offset(x, -STICK_LEN), Offset(x, STICK_LEN), STICK_W, StrokeCap.Round)
    }
}

private const val GRID = 100f
private const val RADIUS = 22f
private const val CX = 50f
private const val CY = 41f
private const val BADGE_R = 23f
private const val UTENSIL_SCALE = 0.62f
private const val SPOON_DEG = 38f
private const val TRAVEL = 70f
private const val STICK_X = 3.75f
private const val STICK_LEN = 27.5f
private const val OUTLINE = 11.25f
private const val STICK_W = 6.25f
