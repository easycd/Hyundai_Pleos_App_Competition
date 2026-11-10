package com.easycd.nextplace.core.pleos.navi

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.constants.NavigationStatus
import ai.pleos.playground.navi.data.CurrentLocationInfo
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.DestinationInfo
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.sdk.NaviController
import com.easycd.nextplace.core.domain.sdk.NaviEvent
import com.easycd.nextplace.core.domain.sdk.RouteTarget
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import com.easycd.nextplace.core.pleos.system.PleosScope
import com.easycd.nextplace.core.pleos.system.connectWithRetry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * NaviHelper SDK 3.0.5 래퍼 (F-10). 목적지(좌표·이름·주소)만 넘기고 poiId 는 비운다.
 * requestRoute 는 결과를 돌려주지 않으므로, 1.5초 안에 onError 가 오지 않으면 성공으로 본다.
 */
@Singleton
class RealNaviController @Inject constructor(
    @ApplicationContext private val context: Context,
    scope: PleosScope,
) : NaviController {
    private val _status = MutableStateFlow(SdkStatus.CONNECTING)
    override val status: StateFlow<SdkStatus> = _status.asStateFlow()
    private val _routeActive = MutableStateFlow(false)
    override val routeActive: StateFlow<Boolean> = _routeActive.asStateFlow()
    private val _roadName = MutableStateFlow<String?>(null)
    override val roadName: StateFlow<String?> = _roadName.asStateFlow()
    private val _events = MutableSharedFlow<NaviEvent>(extraBufferCapacity = 8)
    override val events: Flow<NaviEvent> = _events.asSharedFlow()

    private var navi: NaviHelper? = null
    @Volatile private var pendingDestination: CompletableDeferred<DestinationInfo?>? = null
    @Volatile private var lastError: NaviErrorCode? = null

    private val listener = object : NaviHelperEventListener {
        override fun onDestinationArrived(info: DestinationArrivedInfo) {
            _routeActive.value = false
            _events.tryEmit(NaviEvent.Arrived(info.name))
        }
        override fun onRouteEnded() {
            _routeActive.value = false
            _events.tryEmit(NaviEvent.RouteEnded)
        }
        override fun onRouteCancelled() {
            _routeActive.value = false
            _events.tryEmit(NaviEvent.RouteCancelled)
        }
        override fun onNavigationStatus(status: NavigationStatus) {
            _routeActive.value = status == NavigationStatus.ROUTE_DRIVE
        }
        override fun onCurrentLocationInfo(info: CurrentLocationInfo) {
            _roadName.value = info.address.takeIf { it.isNotBlank() }
        }
        override fun onDestinationInfo(info: DestinationInfo) {
            pendingDestination?.complete(info)
        }
        override fun onError(code: NaviErrorCode) {
            lastError = code
        }
    }

    init {
        scope.connectWithRetry(_status) { connect() }
    }

    private suspend fun connect(): Boolean {
        val helper = navi ?: NaviHelper(context).also { navi = it }
        val ok = suspendCancellableCoroutine { cont ->
            helper.initialize(
                { if (cont.isActive) cont.resume(true) },
                { _ -> if (cont.isActive) cont.resume(false) },
            )
        }
        if (ok) {
            helper.addListener(listener)
            runCatching { helper.getCurrentLocationInfo() }
        }
        return ok
    }

    override suspend fun currentDestination(): RouteTarget? {
        val helper = navi ?: return null
        if (!_routeActive.value) return null
        val waiter = CompletableDeferred<DestinationInfo?>()
        pendingDestination = waiter
        runCatching { helper.getDestinationInfo() }
        val info = withTimeoutOrNull(QUERY_TIMEOUT_MS) { waiter.await() }
        pendingDestination = null
        return info?.let { RouteTarget(GeoPoint(it.latitude, it.longitude), it.name, it.address) }
    }

    override suspend fun requestRoute(target: RouteTarget): Boolean {
        val helper = navi ?: return false
        lastError = null
        val sent = runCatching {
            helper.requestRoute(
                RouteInfo(
                    latitude = target.point.lat,
                    longitude = target.point.lon,
                    poiName = target.name,
                    poiId = "",
                    address = target.address ?: "",
                    poiSubId = "",
                ),
            )
        }.isSuccess
        if (!sent) return false
        delay(ERROR_WINDOW_MS)
        val ok = lastError == null
        if (ok) _routeActive.value = true
        return ok
    }

    private companion object {
        const val QUERY_TIMEOUT_MS = 1_000L
        const val ERROR_WINDOW_MS = 1_500L
    }
}
