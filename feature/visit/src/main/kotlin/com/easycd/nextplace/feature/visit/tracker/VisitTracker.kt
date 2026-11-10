package com.easycd.nextplace.feature.visit.tracker

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.LocationSample
import com.easycd.nextplace.core.domain.model.Visit
import com.easycd.nextplace.core.domain.model.VisitSession
import com.easycd.nextplace.core.domain.model.VisitState
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.repository.VisitRepository
import com.easycd.nextplace.core.domain.repository.VisitSessionRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.LocationReason
import com.easycd.nextplace.core.domain.sdk.LocationSource
import com.easycd.nextplace.core.domain.sdk.NaviController
import com.easycd.nextplace.core.domain.sdk.NaviEvent
import com.easycd.nextplace.feature.visit.note.VisitNoteAsk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 방문 판정 상태 기계 (기술서 7-4, F-11). VisitSession 1행: ROUTING → ARRIVED → STAYING → VISITED → LEFT.
 * 도착은 내비 도착 이벤트 또는 50m 진입, 100m 안 누적 10분이면 방문 기록, 100m 밖 1분이면 출발 → S-15.
 * 세션이 있는 동안 VISIT 사유로 위치를 받는다 (앱이 뒤에 있어도).
 */
@Singleton
class VisitTracker @Inject constructor(
    private val sessions: VisitSessionRepository,
    private val visits: VisitRepository,
    private val places: PlaceRepository,
    private val location: LocationSource,
    private val navi: NaviController,
    private val clock: Clock,
    private val ask: VisitNoteAsk,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = Mutex()
    private var lastSampleAt: Long? = null
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            sessions.observe().map { it != null }.distinctUntilChanged().collect { active ->
                if (active) location.acquire(LocationReason.VISIT) else location.release(LocationReason.VISIT)
            }
        }
        scope.launch { location.samples.collect { onSample(it) } }
        scope.launch { navi.events.collect { onNavi(it) } }
    }

    private suspend fun onNavi(e: NaviEvent) = lock.withLock {
        val s = sessions.get() ?: return@withLock
        val now = clock.now()
        when (e) {
            is NaviEvent.Arrived -> if (s.state == VisitState.ROUTING) {
                sessions.save(s.copy(state = VisitState.ARRIVED, arrivedAt = now, updatedAt = now))
            }
            NaviEvent.RouteCancelled -> if (s.state == VisitState.ROUTING) sessions.clear()
            NaviEvent.RouteEnded -> Unit
        }
    }

    private suspend fun onSample(sample: LocationSample) = lock.withLock {
        val s = sessions.get() ?: return@withLock
        val place = places.get(s.placeId)
        val target = place?.point
        val now = sample.timeMs
        if (target == null) return@withLock sessions.clear()
        val d = Geo.distanceM(sample.point, target)
        val prevSample = lastSampleAt
        lastSampleAt = now
        val next = step(s, d, now, prevSample)
        when {
            next == null -> sessions.clear()
            next.state == VisitState.LEFT -> {
                sessions.clear()
                next.visitId?.let { id ->
                    visits.setLeftAt(id, now)
                    ask.ask(id, place.name ?: place.displayName.orEmpty())
                }
            }
            next != s -> sessions.save(next)
        }
    }

    /** 순수 상태 전이. null = 세션 버림. */
    private suspend fun step(s: VisitSession, d: Double, now: Long, prev: Long?): VisitSession? {
        val inside = d <= Tunables.VISIT_STAY_RADIUS_M
        return when (s.state) {
            VisitState.ROUTING -> when {
                now - s.startedAt > Tunables.VISIT_ROUTING_EXPIRE_MS -> null
                d <= Tunables.VISIT_ARRIVE_RADIUS_M -> s.copy(state = VisitState.ARRIVED, arrivedAt = now, stayStartedAt = now, lastInsideAt = now, updatedAt = now)
                else -> s
            }
            VisitState.ARRIVED, VisitState.STAYING -> stay(s, inside, now, prev)
            VisitState.VISITED -> {
                val out = s.outsideSince
                when {
                    inside -> s.copy(outsideSince = null, lastInsideAt = now, updatedAt = now)
                    out == null -> s.copy(outsideSince = now, updatedAt = now)
                    now - out >= Tunables.VISIT_LEFT_MS -> s.copy(state = VisitState.LEFT, updatedAt = now)
                    else -> s
                }
            }
            VisitState.LEFT -> null
        }
    }

    private suspend fun stay(s: VisitSession, inside: Boolean, now: Long, prev: Long?): VisitSession {
        val gapOk = prev == null || now - prev <= Tunables.VISIT_GAP_TOLERANCE_MS
        if (!inside) {
            val since = s.outsideSince ?: now
            return if (now - since > Tunables.VISIT_OUT_RESET_MS) {
                s.copy(state = VisitState.ARRIVED, stayStartedAt = null, outsideSince = since, updatedAt = now)
            } else {
                s.copy(outsideSince = since, updatedAt = now)
            }
        }
        val start = s.stayStartedAt?.takeIf { gapOk } ?: now
        val staying = s.copy(state = VisitState.STAYING, stayStartedAt = start, lastInsideAt = now, outsideSince = null, updatedAt = now)
        if (now - start < Tunables.VISIT_STAY_MS) return staying
        return staying.copy(state = VisitState.VISITED, visitId = recordVisit(s.placeId, s.arrivedAt ?: start))
    }

    /** 같은 가게에 2시간 안에 다시 도착하면 새 Visit 을 만들지 않는다. */
    private suspend fun recordVisit(placeId: String, arrivedAt: Long): String {
        val last = visits.lastFor(placeId)
        if (last != null && arrivedAt - last.arrivedAt <= Tunables.VISIT_SAME_WINDOW_MS) return last.id
        val v = Visit(UUID.randomUUID().toString(), placeId, arrivedAt, leftAt = null, verified = true, note = null)
        visits.insert(v)
        return v.id
    }
}
