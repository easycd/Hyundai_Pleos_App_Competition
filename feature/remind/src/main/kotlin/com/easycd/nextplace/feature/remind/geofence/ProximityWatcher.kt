package com.easycd.nextplace.feature.remind.geofence

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.LocationSample
import com.easycd.nextplace.core.domain.model.Place
import javax.inject.Inject

/** 근접 이벤트: 알림 거리 안에 들어온 가게 (또는 보류 중 1분마다 다시 보는 가게). */
data class ProximityEvent(val place: Place, val distanceM: Double, val ahead: Boolean)

/**
 * 근접 감시 (기술서 7-2 이벤트 원천 2안: 1초 위치 콜백에서 반경에 걸친 격자 칸만 보고 거리 계산).
 * 들어올 때 1회 알리고, 반경 밖으로 100m 더 나가야 벗어난 것으로 본다(경계 흔들림 방지).
 * 보류(식사 시간 밖) 가게는 반경 안에 있는 동안 1분마다 다시 알린다.
 */
class ProximityWatcher @Inject constructor() {
    private var targets: Map<Pair<Int, Int>, List<Place>> = emptyMap()
    private var radiusM = Tunables.REMIND_RADIUS_DEFAULT_M
    private val inside = mutableMapOf<String, Long>()
    private val held = mutableSetOf<String>()

    /** 전부 해제한 뒤 대상만 다시 등록 (앱 시작·설정 변경·가게 확정/수정/삭제/되돌리기/가져오기). */
    @Synchronized
    fun resync(places: List<Place>, radiusM: Int) {
        this.radiusM = radiusM
        targets = places.groupBy { Geo.cell(it.lat!!) to Geo.cell(it.lon!!) }
        val ids = places.map { it.id }.toSet()
        inside.keys.retainAll(ids)
        held.retainAll(ids)
    }

    @Synchronized
    fun clear() {
        targets = emptyMap()
        inside.clear()
        held.clear()
    }

    /** 이 가게는 식사 시간 필터에 걸려 보류: 반경 안에 있는 동안 1분마다 다시 이벤트. */
    @Synchronized
    fun hold(id: String) {
        held += id
    }

    @Synchronized
    fun onSample(s: LocationSample): List<ProximityEvent> {
        if (targets.isEmpty()) return emptyList()
        val range = Geo.cellRange(s.point, (radiusM + Tunables.REMIND_EXIT_MARGIN_M).toDouble())
        val near = buildList {
            for (la in range.latFrom..range.latTo) for (lo in range.lonFrom..range.lonTo) addAll(targets[la to lo].orEmpty())
        }
        val out = mutableListOf<ProximityEvent>()
        val seen = mutableSetOf<String>()
        near.forEach { p ->
            val d = Geo.distanceM(s.point, p.point!!)
            seen += p.id
            val was = inside[p.id]
            when {
                was == null && d <= radiusM -> {
                    inside[p.id] = s.timeMs
                    out += ProximityEvent(p, d, Geo.isAhead(s.point, s.heading.toDouble(), p.point!!))
                }
                was != null && d > radiusM + Tunables.REMIND_EXIT_MARGIN_M -> {
                    inside.remove(p.id)
                    held.remove(p.id)
                }
                was != null && p.id in held && s.timeMs - was >= Tunables.REMIND_RECHECK_MS -> {
                    inside[p.id] = s.timeMs
                    out += ProximityEvent(p, d, Geo.isAhead(s.point, s.heading.toDouble(), p.point!!))
                }
            }
        }
        (inside.keys - seen).toList().forEach { inside.remove(it); held.remove(it) }
        return out
    }
}
