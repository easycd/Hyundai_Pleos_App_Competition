package com.easycd.nextplace.feature.voicesave.pipeline.stage

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.location.LocatedPoint
import com.easycd.nextplace.core.domain.location.Locator
import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.model.TimeCue
import com.easycd.nextplace.core.domain.pipeline.Stage
import javax.inject.Inject

data class LocateInput(val snapshot: LocationSnapshot, val time: TimeCue, val lookbackSec: Int)

/** 4단계 지점 추정 (10ms). 스냅샷만 쓰고 실시간 위치는 다시 읽지 않는다. 위치 없음이면 null. */
class LocateStage @Inject constructor() : Stage<LocateInput, LocatedPoint?> {
    override val name = "4 Locate"
    override suspend fun run(input: LocateInput): LocatedPoint? {
        val extra = if (input.snapshot.source == PlaceSource.GLEO) Tunables.GLEO_EXTRA_WINDOW_MS else 0L
        return Locator.locate(input.snapshot, input.time, input.lookbackSec, extra)
    }
}
