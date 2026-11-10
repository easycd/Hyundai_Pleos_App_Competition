package com.easycd.nextplace.feature.remind.pipeline.filter

import com.easycd.nextplace.core.domain.sdk.NaviController
import com.easycd.nextplace.feature.remind.geofence.ProximityEvent
import javax.inject.Inject

/** 3 내비 목적지 없음 (안내 중이면 묻지 않는다 · 안내 음성과 겹치지 않게). */
class NaviBusyFilter @Inject constructor(private val navi: NaviController) : RemindFilter {
    override val name = "navi"
    override suspend fun pass(e: ProximityEvent, ctx: RemindContext): Boolean = !navi.routeActive.value
}
