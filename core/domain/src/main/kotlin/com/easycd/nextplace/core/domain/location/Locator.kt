package com.easycd.nextplace.core.domain.location

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.LocationSample
import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.TimeCue

/** 지점 추정 결과 (기술서 7-1 4단계 Locate). */
data class LocatedPoint(
    val point: GeoPoint,
    val headingDeg: Double?,
    val targetTimeMs: Long,
    /** t* 앞뒤 5초 안에 표본이 없어 말한 곳 좌표를 쓴 경우. */
    val timeInexact: Boolean,
)

/**
 * 시점 단서로 기준 시각 t*를 정하고 버퍼에서 그 시각의 좌표를 보간한다.
 * '방금'·단서 없음 = T − N (N = 설정 10·20·30초), '아까' = T − 45초, '여기' = T.
 */
object Locator {
    fun targetTime(pressedAtMs: Long, cue: TimeCue, lookbackSec: Int, extraWindowMs: Long = 0L): Long = when (cue) {
        TimeCue.HERE -> pressedAtMs
        TimeCue.EARLIER -> pressedAtMs - Tunables.EARLIER_SEC * MS - extraWindowMs
        TimeCue.JUST, TimeCue.NONE -> pressedAtMs - lookbackSec * MS - extraWindowMs
    }

    fun locate(snapshot: LocationSnapshot, cue: TimeCue, lookbackSec: Int, extraWindowMs: Long = 0L): LocatedPoint? {
        val current = snapshot.current ?: return null
        val t = targetTime(snapshot.pressedAtMs, cue, lookbackSec, extraWindowMs)
        if (cue == TimeCue.HERE) return LocatedPoint(current.point, current.heading.toDouble(), t, false)
        val samples = snapshot.history
        val after = samples.firstOrNull { it.timeMs >= t }
        val before = samples.lastOrNull { it.timeMs <= t }
        val tol = Tunables.LOCATE_TOLERANCE_MS
        return when {
            before != null && after != null && (t - before.timeMs <= tol || after.timeMs - t <= tol) ->
                interpolate(before, after, t)
            before != null && t - before.timeMs <= tol -> LocatedPoint(before.point, before.heading.toDouble(), t, false)
            after != null && after.timeMs - t <= tol -> LocatedPoint(after.point, after.heading.toDouble(), t, false)
            else -> LocatedPoint(current.point, current.heading.toDouble(), t, true)
        }
    }

    private fun interpolate(a: LocationSample, b: LocationSample, t: Long): LocatedPoint {
        if (a.timeMs == b.timeMs) return LocatedPoint(a.point, a.heading.toDouble(), t, false)
        val f = (t - a.timeMs).toDouble() / (b.timeMs - a.timeMs)
        val moved = Geo.distanceM(a.point, b.point) > MIN_MOVE_M
        val heading = if (moved) Geo.bearingDeg(a.point, b.point) else b.heading.toDouble()
        return LocatedPoint(Geo.lerp(a.point, b.point, f), heading, t, false)
    }

    private const val MS = 1_000L
    private const val MIN_MOVE_M = 2.0
}
