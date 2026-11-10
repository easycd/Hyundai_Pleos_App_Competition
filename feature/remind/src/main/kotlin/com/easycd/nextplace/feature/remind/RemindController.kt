package com.easycd.nextplace.feature.remind

import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.repository.SettingsRepository
import com.easycd.nextplace.core.domain.sdk.LocationReason
import com.easycd.nextplace.core.domain.sdk.LocationSource
import com.easycd.nextplace.feature.remind.geofence.ProximityWatcher
import com.easycd.nextplace.feature.remind.geofence.RemindEligibility
import com.easycd.nextplace.feature.remind.pipeline.RemindPipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 리마인드 수명 (기술서 7-2 감시 다시 등록, 7-4 5 위치 수명). 리마인드가 켜져 있으면 REMIND 사유로 위치를 받고,
 * 가게 목록·설정이 바뀔 때마다 감시를 전부 해제한 뒤 대상만 다시 등록한다. 끄면 전부 해제.
 */
@Singleton
class RemindController @Inject constructor(
    private val places: PlaceRepository,
    private val settings: SettingsRepository,
    private val location: LocationSource,
    private val watcher: ProximityWatcher,
    private val pipeline: RemindPipeline,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            settings.settings.map { it.remindEnabled }.distinctUntilChanged().collect { on ->
                if (on) location.acquire(LocationReason.REMIND) else location.release(LocationReason.REMIND)
            }
        }
        scope.launch {
            combine(places.observeAll(), settings.settings) { list, s ->
                if (!s.remindEnabled) null else list.map { it.place }.filter { RemindEligibility.eligible(it, s) } to s.remindRadiusM
            }.collect { target ->
                if (target == null) watcher.clear() else watcher.resync(target.first, target.second)
            }
        }
        scope.launch {
            location.samples.collect { s ->
                val events = watcher.onSample(s)
                if (events.isNotEmpty()) launch { pipeline.handle(events) }
            }
        }
    }
}
