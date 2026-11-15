package com.easycd.nextplace.core.pleos.vehicle

import ai.pleos.playground.vehicle.Vehicle
import android.content.Context
import com.easycd.nextplace.core.domain.pipeline.TraceLog
import com.easycd.nextplace.core.domain.sdk.UxRestrictionSource
import com.easycd.nextplace.core.pleos.BuildConfig
import com.easycd.nextplace.core.pleos.system.PleosScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Vehicle SDK 2.3.2 진단 로그 (기획서 9장 SDK 표: 진단 로그, 기술서 8장 '주행 제한': Brake 는 로그에만 남긴다).
 * 주행 제한 상태가 바뀔 때마다 그 순간의 브레이크 페달·주행 모드를 로그 파일에 남긴다. 주행 제한 판단(F-14)에는 쓰지 않는다.
 * SDK 가 없거나 연결이 안 돼도 앱 동작에는 영향이 없다 (가짜 SDK 빌드에서는 켜지 않음).
 */
@Singleton
class VehicleLog @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ux: UxRestrictionSource,
    private val trace: TraceLog,
    private val scope: PleosScope,
) {
    private var vehicle: Vehicle? = null

    fun start() {
        if (BuildConfig.FAKE_SDK || vehicle != null) return
        vehicle = runCatching { Vehicle(context).also { it.initialize() } }
            .onFailure { trace.error(TAG, it) }
            .getOrNull() ?: return
        scope.launch {
            ux.restricted.collect { restricted -> snapshot(restricted) }
        }
    }

    private fun snapshot(restricted: Boolean?) {
        val v = vehicle ?: return
        runCatching {
            v.getBrake().getCurrentBrakePedalStatus(
                { on -> trace.event(TAG, "ux restricted=$restricted brakePedal=$on") },
                { e -> trace.event(TAG, "ux restricted=$restricted brakePedal=? (${e.javaClass.simpleName})") },
            )
            v.getDrivingMode().getCurrentDrivingModeState(
                { mode -> trace.event(TAG, "ux restricted=$restricted drivingMode=$mode") },
                { e -> trace.event(TAG, "ux restricted=$restricted drivingMode=? (${e.javaClass.simpleName})") },
            )
        }.onFailure { trace.error(TAG, it) }
    }

    private companion object {
        const val TAG = "vehicle"
    }
}
