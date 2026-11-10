package com.easycd.nextplace.core.pleos.location

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.location.LocationBuffer
import com.easycd.nextplace.core.domain.model.LocationSample
import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.LocationReason
import com.easycd.nextplace.core.domain.sdk.LocationSource
import com.easycd.nextplace.core.domain.sdk.PermissionChecker
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import com.easycd.nextplace.core.pleos.system.PleosScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 위치 버퍼를 채우고 '말한 곳'을 고정한다 (F-02, 기술서 8장).
 * 위치를 받는 이유(앞에 있음·리마인드·방문)가 하나라도 있는 동안만 콜백을 등록하고,
 * 앱이 뒤에 있을 때(리마인드·방문)는 위치 포그라운드 서비스를 띄운다 (기술서 7-4 5).
 */
@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val feed: LocationFeed,
    private val clock: Clock,
    private val permissions: PermissionChecker,
    private val scope: PleosScope,
) : LocationSource {
    private val buffer = LocationBuffer()
    private val reasons = mutableSetOf<LocationReason>()
    private var job: Job? = null
    private val snapshotIds = AtomicLong()
    private val _latest = MutableStateFlow<LocationSample?>(null)
    private val _samples = MutableSharedFlow<LocationSample>(extraBufferCapacity = 16)

    override val status: StateFlow<SdkStatus> get() = feed.status
    override val latest: StateFlow<LocationSample?> = _latest.asStateFlow()
    override val samples: Flow<LocationSample> = _samples.asSharedFlow()
    override val isSimulated: Boolean get() = feed.simulated

    override fun hasPermission(): Boolean = permissions.hasLocation()

    override fun capture(source: PlaceSource): LocationSnapshot {
        val now = clock.now()
        val last = buffer.latest()
        val fresh = last != null && now - last.timeMs <= Tunables.LOCATION_STALE_MS && hasPermission()
        return LocationSnapshot(
            id = snapshotIds.incrementAndGet(),
            pressedAtMs = now,
            current = if (fresh) last else null,
            history = if (fresh) buffer.snapshot(now) else emptyList(),
            source = source,
        )
    }

    @Synchronized
    override fun acquire(reason: LocationReason) {
        reasons += reason
        ensureRunning()
        updateKeeper()
    }

    @Synchronized
    override fun release(reason: LocationReason) {
        reasons -= reason
        if (reasons.isEmpty()) {
            job?.cancel()
            job = null
        }
        updateKeeper()
    }

    private fun ensureRunning() {
        if (job?.isActive == true || !hasPermission()) return
        job = scope.launch {
            feed.updates().collect { s ->
                if (buffer.add(s)) {
                    _latest.value = s
                    _samples.tryEmit(s)
                }
            }
        }
    }

    /** 리마인드·방문 사유가 있으면 포그라운드 서비스, 없으면 끈다. */
    private fun updateKeeper() {
        val background = reasons.any { it != LocationReason.FOREGROUND }
        val intent = Intent(context, LocationKeeperService::class.java)
        if (background) {
            runCatching { ContextCompat.startForegroundService(context, intent) }
        } else {
            context.stopService(intent)
        }
    }

    /** 권한이 새로 생기면 다시 시도한다. */
    @Synchronized
    fun retry() {
        if (reasons.isNotEmpty()) ensureRunning()
    }
}
