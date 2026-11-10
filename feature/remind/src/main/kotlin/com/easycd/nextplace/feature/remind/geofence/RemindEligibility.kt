package com.easycd.nextplace.feature.remind.geofence

import com.easycd.nextplace.core.domain.model.AppSettings
import com.easycd.nextplace.core.domain.model.LocationQuality
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceStatus

/** 알림 대상 (기술서 7-2): 전역 리마인드 켜짐 · 가게별 근처 알림 · 확인됨 · POI 좌표 · 삭제 예정 아님. */
object RemindEligibility {
    fun eligible(p: Place, s: AppSettings): Boolean =
        s.remindEnabled && p.remindEnabled && p.status == PlaceStatus.CONFIRMED &&
            p.locationQuality == LocationQuality.POI && p.deletedAt == null && !p.draft && p.point != null
}
