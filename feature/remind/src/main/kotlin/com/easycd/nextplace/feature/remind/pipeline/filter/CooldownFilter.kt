package com.easycd.nextplace.feature.remind.pipeline.filter

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.feature.remind.geofence.ProximityEvent
import javax.inject.Inject

/** 4 직전 안내 후 10분 지남 (전체 10분 1회). */
class CooldownFilter @Inject constructor() : RemindFilter {
    override val name = "cooldown"
    override suspend fun pass(e: ProximityEvent, ctx: RemindContext): Boolean {
        val last = ctx.settings.lastRemindAt ?: return true
        return ctx.nowMs - last >= Tunables.REMIND_GLOBAL_GAP_MS
    }
}
