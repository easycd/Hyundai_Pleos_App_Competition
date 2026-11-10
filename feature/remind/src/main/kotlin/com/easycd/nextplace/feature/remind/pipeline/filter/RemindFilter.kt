package com.easycd.nextplace.feature.remind.pipeline.filter

import com.easycd.nextplace.core.domain.model.AppSettings
import com.easycd.nextplace.feature.remind.geofence.ProximityEvent

data class RemindContext(val nowMs: Long, val settings: AppSettings)

/** 리마인드 필터 하나 (기술서 6장 확장 지점). 걸리면 이유만 로그로 남기고 소리 없이 끝난다. */
interface RemindFilter {
    val name: String
    suspend fun pass(e: ProximityEvent, ctx: RemindContext): Boolean
}
