package com.easycd.nextplace.feature.remind.geofence

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.repository.SettingsRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 빈도 상한 (기술서 7-2): 같은 가게 하루 1회 + 전체 10분 1회를 뮤텍스 하나로 함께 확인하고,
 * TTS 를 시작하는 순간 두 값을 함께 갱신한다. 동시에 온 이벤트는 차례로 처리되어 하나만 통과한다.
 */
@Singleton
class RemindGate @Inject constructor(
    private val places: PlaceRepository,
    private val settings: SettingsRepository,
    private val clock: Clock,
) {
    private val mutex = Mutex()

    suspend fun tryAcquire(placeId: String): Boolean = mutex.withLock {
        val now = clock.now()
        val p = places.get(placeId) ?: return false
        val s = settings.current()
        if (p.lastRemindedAt?.let { clock.dayIndex(it) == clock.dayIndex(now) } == true) return false
        if (s.lastRemindAt?.let { now - it < Tunables.REMIND_GLOBAL_GAP_MS } == true) return false
        places.setLastReminded(placeId, now)
        settings.update { it.copy(lastRemindAt = now) }
        true
    }
}
