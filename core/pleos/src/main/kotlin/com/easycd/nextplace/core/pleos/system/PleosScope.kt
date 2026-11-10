package com.easycd.nextplace.core.pleos.system

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/** SDK 래퍼들이 쓰는 앱 범위 코루틴. */
@Singleton
class PleosScope @Inject constructor() : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

/**
 * SDK 연결: 실패하면 2·4·8초 간격으로 3회 다시 시도하고, 그래도 안 되면 UNAVAILABLE (기술서 10장).
 * [connect]는 성공하면 true. 한 번의 시도가 [Tunables.SDK_CONNECT_TIMEOUT_MS] 안에 답(초기화 콜백)이 없으면 실패로 보고
 * 다음 시도로 넘어간다 (콜백이 영영 안 오면 CONNECTING 에 머무르지 않게). 시간이 지나 온 콜백은 무시된다.
 */
fun CoroutineScope.connectWithRetry(status: MutableStateFlow<SdkStatus>, connect: suspend () -> Boolean) {
    suspend fun attempt(): Boolean = runCatching { withTimeoutOrNull(Tunables.SDK_CONNECT_TIMEOUT_MS) { connect() } ?: false }.getOrDefault(false)
    launch {
        status.value = SdkStatus.CONNECTING
        if (attempt()) {
            status.value = SdkStatus.READY
            return@launch
        }
        for (wait in Tunables.SDK_RECONNECT_DELAYS_MS) {
            delay(wait)
            if (attempt()) {
                status.value = SdkStatus.READY
                return@launch
            }
        }
        status.value = SdkStatus.UNAVAILABLE
    }
}
