package com.easycd.nextplace.core.domain.geo

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.Side
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

/** 거리·방위·격자 계산 (기술서 9장). 순수 함수만 둔다. */
object Geo {
    private const val EARTH_RADIUS_M = 6_371_000.0
    private const val FULL_CIRCLE = 360.0
    private const val HALF_CIRCLE = 180.0

    fun distanceM(a: GeoPoint, b: GeoPoint): Double {
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * EARTH_RADIUS_M * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }

    /** a에서 b를 본 방위 (북=0, 시계 방향, 0~360). */
    fun bearingDeg(a: GeoPoint, b: GeoPoint): Double {
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return normalize(Math.toDegrees(atan2(y, x)))
    }

    fun normalize(deg: Double): Double = ((deg % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE

    /** 진행 방향 [headingDeg] 기준 대상의 상대 각도 (-180~180, 오른쪽이 +). */
    fun relativeAngle(headingDeg: Double, bearingDeg: Double): Double {
        var d = normalize(bearingDeg - headingDeg)
        if (d > HALF_CIRCLE) d -= FULL_CIRCLE
        return d
    }

    fun sideOf(from: GeoPoint, headingDeg: Double?, target: GeoPoint): Side {
        if (headingDeg == null) return Side.UNKNOWN
        val rel = relativeAngle(headingDeg, bearingDeg(from, target))
        return when {
            abs(rel) < 1.0 || abs(rel) > HALF_CIRCLE - 1.0 -> Side.UNKNOWN
            rel > 0 -> Side.RIGHT
            else -> Side.LEFT
        }
    }

    /** 가게가 진행 방향 ±45° 안이면 '앞' (기획서 4장 흐름 B). */
    fun isAhead(from: GeoPoint, headingDeg: Double?, target: GeoPoint): Boolean {
        if (headingDeg == null) return false
        return abs(relativeAngle(headingDeg, bearingDeg(from, target))) <= Tunables.AHEAD_HALF_ANGLE_DEG
    }

    /** 위도·경도를 0.01도 칸으로 내림 (기술서 8장 격자 컬럼). */
    fun cell(value: Double): Int = floor(value * Tunables.GRID_CELLS_PER_DEGREE).toInt()

    /** 반경 [radiusM]에 걸친 모든 칸 범위 (기술서 9장 격자 조회). 한 칸만 읽으면 경계 가게를 놓친다. */
    fun cellRange(center: GeoPoint, radiusM: Double): CellRange {
        val dLat = radiusM / Tunables.METERS_PER_DEGREE_LAT
        val dLon = radiusM / (Tunables.METERS_PER_DEGREE_LAT * cos(Math.toRadians(center.lat)).coerceAtLeast(MIN_COS))
        return CellRange(
            latFrom = cell(center.lat - dLat),
            latTo = cell(center.lat + dLat),
            lonFrom = cell(center.lon - dLon),
            lonTo = cell(center.lon + dLon),
        )
    }

    /** a→b 사이를 비율 [f]로 보간한 점. */
    fun lerp(a: GeoPoint, b: GeoPoint, f: Double): GeoPoint =
        GeoPoint(a.lat + (b.lat - a.lat) * f, a.lon + (b.lon - a.lon) * f)

    private const val MIN_COS = 0.01
}

data class CellRange(val latFrom: Int, val latTo: Int, val lonFrom: Int, val lonTo: Int) {
    val cellCount: Int get() = (latTo - latFrom + 1) * (lonTo - lonFrom + 1)
}
