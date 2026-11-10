package com.easycd.nextplace.core.domain.pipeline

import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.sdk.LocationSource
import javax.inject.Inject

/**
 * 1단계 위치 고정 (기술서 7-1). 버튼을 누른 순간 현재 위치와 최근 60초 버퍼를 '말한 곳'으로 복사한다.
 * 메모리 복사뿐이라 0ms. 마지막 위치가 3초보다 오래됐거나 권한이 없으면 '위치 없음' 스냅샷.
 * 추천(7-3)·방문 메모·위치만 저장도 이 단계를 같이 쓴다.
 */
class CaptureStage @Inject constructor(private val location: LocationSource) : Stage<PlaceSource, LocationSnapshot> {
    override val name = "1 Capture"
    override suspend fun run(input: PlaceSource): LocationSnapshot = location.capture(input)
}
