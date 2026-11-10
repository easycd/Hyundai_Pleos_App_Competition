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
        val samples = continuousTail(snapshot.history)
        val after = samples.firstOrNull { it.timeMs >= t }
        val before = samples.lastOrNull { it.timeMs <= t }
        val tol = Tunables.LOCATE_TOLERANCE_MS
        return when {
            before != null && after != null && (t - before.timeMs <= tol || after.timeMs - t <= tol) ->
                interpolate(before, after, t)
            before != null && t - before.timeMs <= tol -> LocatedPoint(before.point, before.heading.toDouble(), t, false)
            after != null && after.timeMs - t <= tol -> LocatedPoint(after.point, after.heading.toDouble(), t, false)
            // 버퍼가 t* 까지 닿지 않음 (막 켰거나 위치가 순간이동함): t* 에 가장 가까운 가장 오래된 표본, 없으면 말한 곳
            else -> samples.firstOrNull()?.takeIf { it.timeMs > t }?.let { LocatedPoint(it.point, it.heading.toDouble(), t, true) }
                ?: LocatedPoint(current.point, current.heading.toDouble(), t, true)
        }
    }

    /**
     * 가장 최근 표본부터 거꾸로 이어진 구간만 쓴다. 두 표본 사이가 차로 갈 수 없는 속도([MAX_SPEED_MPS] 초과)면
     * 위치가 튄 것(GPS 오류 · 가짜 경로가 처음으로 돌아감)이라 그 앞은 버린다. 안 그러면 '방금 지난' 가게를 1km 앞에서 찾는다.
     */
    fun continuousTail(samples: List<LocationSample>): List<LocationSample> {
        if (samples.size < 2) return samples
        var from = samples.size - 1
        while (from > 0) {
            val a = samples[from - 1]
            val b = samples[from]
            val dt = (b.timeMs - a.timeMs).coerceAtLeast(1L) / MS.toDouble()
            val d = Geo.distanceM(a.point, b.point)
            if (d > JUMP_MIN_M && d / dt > MAX_SPEED_MPS) break
            from--
        }
        return samples.subList(from, samples.size)
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
    private const val JUMP_MIN_M = 50.0
    /** 250km/h. 이보다 빠르게 움직인 두 표본 사이는 이어진 길이 아니다. */
    private const val MAX_SPEED_MPS = 70.0
}
