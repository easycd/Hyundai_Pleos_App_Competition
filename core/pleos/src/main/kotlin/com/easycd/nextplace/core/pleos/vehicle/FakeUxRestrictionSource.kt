package com.easycd.nextplace.core.pleos.vehicle

import com.easycd.nextplace.core.domain.sdk.UxRestrictionSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 가짜 주행 제한: 정차 상태. 1차 시연 영상은 주행 제한을 끈 개발 빌드로 찍는다 (기획서 11장). */
@Singleton
class FakeUxRestrictionSource @Inject constructor() : UxRestrictionSource {
    override val restricted: StateFlow<Boolean?> = MutableStateFlow(false)
}
