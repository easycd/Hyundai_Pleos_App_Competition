package com.easycd.nextplace.feature.visit.tracker

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.LocationSample
import com.easycd.nextplace.core.domain.model.Visit
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
        when (val r = VisitStep.next(s, d, now, prevSample)) {
            VisitStepResult.Drop -> sessions.clear()
            is VisitStepResult.Keep -> if (r.session != s) sessions.save(r.session)
            is VisitStepResult.Record -> sessions.save(r.session.copy(visitId = recordVisit(s.placeId, r.session.arrivedAt ?: r.session.stayStartedAt ?: now)))
            is VisitStepResult.Left -> {
                sessions.clear()
                r.session.visitId?.let { id ->
                    visits.setLeftAt(id, now)
                    ask.ask(id, place.name ?: place.displayName.orEmpty())
                }
            }
        }
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
