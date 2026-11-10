package com.easycd.nextplace.core.pleos.navi

import com.easycd.nextplace.core.domain.sdk.NaviController
import com.easycd.nextplace.core.domain.sdk.NaviEvent
import com.easycd.nextplace.core.domain.sdk.RouteTarget
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 가짜 내비: 목적지를 기억만 한다. 도착은 VisitTracker 가 목적지 50m 진입으로 판정한다 (기획서 12장 대체). */
@Singleton
class FakeNaviController @Inject constructor() : NaviController {
    override val status: StateFlow<SdkStatus> = MutableStateFlow(SdkStatus.READY)
    private val _routeActive = MutableStateFlow(false)
    override val routeActive: StateFlow<Boolean> = _routeActive.asStateFlow()
    override val roadName: StateFlow<String?> = MutableStateFlow(null)
    override val events: Flow<NaviEvent> = MutableSharedFlow()
    private var destination: RouteTarget? = null

    override suspend fun currentDestination(): RouteTarget? = destination

    override suspend fun requestRoute(target: RouteTarget): Boolean {
        destination = target
        _routeActive.value = true
        return true
    }
}
