package com.easycd.nextplace.feature.remind.pipeline.filter

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.feature.remind.geofence.ProximityEvent
import javax.inject.Inject

/** 1 식사 시간대 11~14 · 17~20시 (설정 "식사 시간에만 알림", 기본 켜짐). */
class MealTimeFilter @Inject constructor(private val clock: Clock) : RemindFilter {
    override val name = "meal"
    override suspend fun pass(e: ProximityEvent, ctx: RemindContext): Boolean {
        if (!ctx.settings.mealTimeOnly) return true
        val h = clock.hourOfDay(ctx.nowMs)
        return Tunables.MEAL_HOURS.any { h in it }
    }
}
