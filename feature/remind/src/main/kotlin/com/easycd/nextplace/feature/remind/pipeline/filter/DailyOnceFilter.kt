package com.easycd.nextplace.feature.remind.pipeline.filter

import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.feature.remind.geofence.ProximityEvent
import javax.inject.Inject

/** 2 오늘 이 가게를 안내하지 않았음 (같은 가게 하루 1회, 날짜 경계는 차량 시간 0시). */
class DailyOnceFilter @Inject constructor(private val clock: Clock) : RemindFilter {
    override val name = "daily"
    override suspend fun pass(e: ProximityEvent, ctx: RemindContext): Boolean {
        val last = e.place.lastRemindedAt ?: return true
        return clock.dayIndex(last) != clock.dayIndex(ctx.nowMs)
    }
}
