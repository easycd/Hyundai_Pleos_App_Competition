package com.easycd.nextplace.core.pleos.location

import com.easycd.nextplace.core.domain.model.LocationSample
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** 위치 원천 (FusedLocation SDK 또는 가짜 경로 재생). 모으는 동안만 콜백을 등록한다. */
interface LocationFeed {
    val status: StateFlow<SdkStatus>
    val simulated: Boolean
    fun updates(): Flow<LocationSample>
}
