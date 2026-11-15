package com.easycd.nextplace.start

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.easycd.nextplace.core.domain.contract.AppForeground
import com.easycd.nextplace.core.domain.pipeline.TraceLog
import com.easycd.nextplace.core.domain.repository.SettingsRepository
import com.easycd.nextplace.core.domain.sdk.LocationReason
import com.easycd.nextplace.core.domain.sdk.LocationSource
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.pleos.vehicle.VehicleLog
import com.easycd.nextplace.feature.remind.RemindController
import com.easycd.nextplace.feature.visit.tracker.VisitTracker
import com.easycd.nextplace.feature.voicesave.recovery.SaveRecovery
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 시작 (기술서 9장: 시작 불러오기 0.8초, Hilt 지연 주입). 메인 스레드에서는 생명주기 관찰만 걸고,
 * SDK 래퍼·리마인드·방문 판정·복구는 뒤 스레드에서 만든다.
 * 앱 창이 보이는 동안 FOREGROUND 사유로 위치를 받는다 (7-4 위치 수명).
 */
@Singleton
class AppStartup @Inject constructor(
    private val foreground: AppForeground,
    private val location: Lazy<LocationSource>,
    private val settings: Lazy<SettingsRepository>,
    private val tts: Lazy<SpeechSynthesizer>,
    private val remind: Lazy<RemindController>,
    private val visit: Lazy<VisitTracker>,
    private val recovery: Lazy<SaveRecovery>,
    private val vehicle: Lazy<VehicleLog>,
    private val trace: TraceLog,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun run() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                foreground.set(true)
                scope.launch { location.get().acquire(LocationReason.FOREGROUND) }
            }

            override fun onStop(owner: LifecycleOwner) {
                foreground.set(false)
                scope.launch { location.get().release(LocationReason.FOREGROUND) }
            }
        })
        scope.launch {
            runCatching { recovery.get().run() }.onFailure { trace.error("startup/recovery", it) }
        }
        scope.launch {
            runCatching { tts.get().setVolume(settings.get().settings.first().voiceVolume) }
            remind.get().start()
            visit.get().start()
            vehicle.get().start()
            trace.event("startup", "ready")
        }
    }
}
