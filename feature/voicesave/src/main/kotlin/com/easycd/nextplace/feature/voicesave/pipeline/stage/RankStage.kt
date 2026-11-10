package com.easycd.nextplace.feature.voicesave.pipeline.stage

import com.easycd.nextplace.core.domain.location.LocatedPoint
import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.pipeline.Stage
import com.easycd.nextplace.feature.voicesave.rank.CandidateScorer
import javax.inject.Inject

data class RankInput(val candidates: List<PoiCandidate>, val located: LocatedPoint, val cues: Cues)

/** 6단계 점수순 상위 5 (20ms). */
class RankStage @Inject constructor(private val scorer: CandidateScorer) : Stage<RankInput, List<PoiCandidate>> {
    override val name = "6 Rank"
    override suspend fun run(input: RankInput): List<PoiCandidate> = scorer.rank(input.candidates, input.located, input.cues)
}
