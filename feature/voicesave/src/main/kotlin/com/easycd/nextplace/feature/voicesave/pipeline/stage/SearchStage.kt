package com.easycd.nextplace.feature.voicesave.pipeline.stage

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.pipeline.Stage
import com.easycd.nextplace.core.domain.poi.PoiQuery
import com.easycd.nextplace.core.domain.poi.PoiResult
import com.easycd.nextplace.core.domain.poi.PoiSource
import javax.inject.Inject

/** 5단계 POI 검색: 반경 150m, 최대 15건, 1.2초 제한 (넘기면 미확인 저장). */
class SearchStage @Inject constructor(private val poi: PoiSource) : Stage<PoiQuery, PoiResult> {
    override val name = "5 Search"
    override val timeoutMs = Tunables.POI_TIMEOUT_MS
    override suspend fun run(input: PoiQuery): PoiResult = poi.search(input)
}
