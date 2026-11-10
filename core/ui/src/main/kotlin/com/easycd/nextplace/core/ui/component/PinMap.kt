package com.easycd.nextplace.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.ui.theme.Np
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max

enum class PinTone { SAVED, VISITED, UNCONFIRMED, CANDIDATE, ESTIMATED }

@Immutable
data class MapPin(val id: String, val point: GeoPoint, val tone: PinTone, val selected: Boolean = false)

/**
 * 핀 지도 (S-07 · S-08 · S-09 · S-14 · S-16). 카카오맵 SDK 가 차량에서 뜨는지 확인 전이라
 * 프로토타입은 배경 지도 없이 핀과 내 위치만 상대 위치로 그린다 (화면 설계서 8장: 지도가 없으면 비움).
 * 핀 색은 상태 칩과 같다: 저장 주황 · 방문 초록 · 미확인 노랑(+?).
 */
@Composable
fun PinMap(pins: List<MapPin>, me: GeoPoint?, modifier: Modifier = Modifier, onPin: ((String) -> Unit)? = null) {
    val c = Np.colors
    val all = pins.map { it.point } + listOfNotNull(me)
    Canvas(
        modifier.glass(RoundedCornerShape(36.dp), elevation = 0.dp).pointerInput(pins, me, onPin) {
            if (onPin != null) {
                detectTapGestures { tap ->
                    // 핀이 없으면(필터 결과 0 · 위치만 있음) 누를 것이 없다. 빈 목록으로 투영을 만들면 minOf 가 던진다.
                    if (pins.isEmpty() || all.isEmpty()) return@detectTapGestures
                    val proj = Projection(all, size.width.toFloat(), size.height.toFloat())
                    pins.minByOrNull { p -> proj.of(p.point).let { hypot(it.x - tap.x, it.y - tap.y) } }
                        ?.takeIf { p -> proj.of(p.point).let { hypot(it.x - tap.x, it.y - tap.y) } < TAP_PX }
                        ?.let { onPin(it.id) }
                }
            }
        },
    ) {
        drawGrid(c.glassBorder)
        if (all.isEmpty()) return@Canvas
        val proj = Projection(all, size.width, size.height)
        me?.let {
            val o = proj.of(it)
            drawCircle(c.brand.copy(alpha = 0.18f), 28.dp.toPx(), o)
            drawCircle(Color.White, 12.dp.toPx(), o)
            drawCircle(c.brand, 8.dp.toPx(), o)
        }
        pins.sortedBy { it.selected }.forEach { p ->
            val color = when (p.tone) {
                PinTone.SAVED, PinTone.CANDIDATE -> c.brand
                PinTone.VISITED -> c.green
                PinTone.UNCONFIRMED, PinTone.ESTIMATED -> c.amber
            }
            drawPin(proj.of(p.point), color, if (p.selected) SELECTED_SCALE else 1f, p.tone == PinTone.UNCONFIRMED, c.background)
        }
    }
}

private class Projection(points: List<GeoPoint>, private val w: Float, private val h: Float) {
    private val minLat = points.minOf { it.lat }
    private val maxLat = points.maxOf { it.lat }
    private val minLon = points.minOf { it.lon }
    private val maxLon = points.maxOf { it.lon }
    private val k = cos(Math.toRadians((minLat + maxLat) / 2))
    private val spanLat = max(maxLat - minLat, MIN_SPAN)
    private val spanLon = max((maxLon - minLon) * k, MIN_SPAN)
    private val scale = minOf((w - PAD * 2) / spanLon, (h - PAD * 2) / spanLat)
    private val cx = (minLon + maxLon) / 2
    private val cy = (minLat + maxLat) / 2

    fun of(p: GeoPoint) = Offset(
        (w / 2 + (p.lon - cx) * k * scale).toFloat(),
        (h / 2 - (p.lat - cy) * scale).toFloat(),
    )

    private companion object {
        const val PAD = 72f
        const val MIN_SPAN = 0.002
    }
}

private fun DrawScope.drawGrid(color: Color) {
    val step = 64.dp.toPx()
    var x = step
    while (x < size.width) {
        drawLine(color, Offset(x, 0f), Offset(x, size.height), 1f)
        x += step
    }
    var y = step
    while (y < size.height) {
        drawLine(color, Offset(0f, y), Offset(size.width, y), 1f)
        y += step
    }
}

private fun DrawScope.drawPin(at: Offset, color: Color, scale: Float, question: Boolean, hole: Color) {
    val r = 18.dp.toPx() * scale
    val path = Path().apply {
        moveTo(at.x, at.y)
        cubicTo(at.x - r * 0.6f, at.y - r * 1.2f, at.x - r * 1.1f, at.y - r * 1.6f, at.x - r * 1.1f, at.y - r * 2.2f)
        cubicTo(at.x - r * 1.1f, at.y - r * 3.4f, at.x + r * 1.1f, at.y - r * 3.4f, at.x + r * 1.1f, at.y - r * 2.2f)
        cubicTo(at.x + r * 1.1f, at.y - r * 1.6f, at.x + r * 0.6f, at.y - r * 1.2f, at.x, at.y)
        close()
    }
    drawPath(path, color)
    drawPath(path, Color.White.copy(alpha = 0.6f), style = Stroke(2.dp.toPx()))
    drawCircle(if (question) hole else Color.White, r * 0.45f, Offset(at.x, at.y - r * 2.2f))
    if (question) drawCircle(color, r * 0.18f, Offset(at.x, at.y - r * 2.2f))
}

/** 두 점 사이 거리(m) — 지도 밖에서도 쓰는 짧은 도우미. */
fun distanceM(a: GeoPoint, b: GeoPoint): Double = Geo.distanceM(a, b)

private const val TAP_PX = 64f
private const val SELECTED_SCALE = 1.2f
