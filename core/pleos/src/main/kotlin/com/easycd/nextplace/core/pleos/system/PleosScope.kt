package com.easycd.nextplace.core.pleos.system

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** SDK 래퍼들이 쓰는 앱 범위 코루틴. */
@Singleton
class PleosScope @Inject constructor() : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

/**
 * SDK 연결: 실패하면 2·4·8초 간격으로 3회 다시 시도하고, 그래도 안 되면 UNAVAILABLE (기술서 10장).
 * [connect]는 성공하면 true.
 */
fun CoroutineScope.connectWithRetry(status: MutableStateFlow<SdkStatus>, connect: suspend () -> Boolean) {
    launch {
        status.value = SdkStatus.CONNECTING
        if (runCatching { connect() }.getOrDefault(false)) {
            status.value = SdkStatus.READY
            return@launch
        }
        for (wait in Tunables.SDK_RECONNECT_DELAYS_MS) {
            delay(wait)
            if (runCatching { connect() }.getOrDefault(false)) {
                status.value = SdkStatus.READY
                return@launch
            }
        }
        status.value = SdkStatus.UNAVAILABLE
    }
}
