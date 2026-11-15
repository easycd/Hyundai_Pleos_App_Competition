package com.easycd.nextplace.feature.voicesave.pipeline

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.location.LocatedPoint
import com.easycd.nextplace.core.domain.location.Locator
import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.pipeline.StageOutcome
import com.easycd.nextplace.core.domain.pipeline.StageRunner
import com.easycd.nextplace.core.domain.poi.PoiQuery
import com.easycd.nextplace.core.domain.poi.PoiResult
import com.easycd.nextplace.core.domain.voice.Categories
import com.easycd.nextplace.core.domain.voice.Intent
import com.easycd.nextplace.core.domain.voice.IntentParser
import com.easycd.nextplace.core.domain.voice.ParsedUtterance
import com.easycd.nextplace.core.domain.voice.VoiceStage
import com.easycd.nextplace.feature.voicesave.pipeline.stage.ConfirmInput
import com.easycd.nextplace.feature.voicesave.pipeline.stage.ConfirmResult
import com.easycd.nextplace.feature.voicesave.pipeline.stage.ConfirmStage
import com.easycd.nextplace.feature.voicesave.pipeline.stage.LocateInput
import com.easycd.nextplace.feature.voicesave.pipeline.stage.LocateStage
import com.easycd.nextplace.feature.voicesave.pipeline.stage.PersistKind
import com.easycd.nextplace.feature.voicesave.pipeline.stage.RankInput
import com.easycd.nextplace.feature.voicesave.pipeline.stage.RankStage
import com.easycd.nextplace.feature.voicesave.pipeline.stage.SearchStage
import com.easycd.nextplace.feature.voicesave.rank.CandidateScorer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 4 Locate → 5 Search → 6 Rank → 7 Confirm (기술서 7-1). 결과는 저장 방식(확정·미확인), 취소면 null.
 * 듣는 동안 부분 결과로 의도가 저장이고 업종·이름이 나오면 0.3초 디바운스 뒤 검색을 먼저 시작하고,
 * 최종 문장의 검색 조건과 같을 때만 그 결과를 쓴다 (한 발화의 POI 요청 최대 2회).
 */
class FindAndConfirm @Inject constructor(
    private val runner: StageRunner,
    private val locate: LocateStage,
    private val search: SearchStage,
    private val rank: RankStage,
    private val confirm: ConfirmStage,
    private val scorer: CandidateScorer,
    private val parser: IntentParser,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var window: Pair<LocationSnapshot, Int>? = null
    private var debounce: Job? = null
    private var prefetch: Pair<PoiQuery, Deferred<StageOutcome<PoiResult>>>? = null
    private var prefetchUsed = false

    fun startPrefetchWindow(snapshot: LocationSnapshot, lookbackSec: Int) {
        clearPrefetch()
        window = snapshot to lookbackSec
        if (snapshot.hasLocation) scope.launch { search.warmUp() }
    }

    fun onPartial(text: String) {
        val (snap, lookback) = window ?: return
        if (prefetchUsed || !snap.hasLocation) return
        debounce?.cancel()
        debounce = scope.launch {
            delay(Tunables.PREFETCH_DEBOUNCE_MS)
            val parsed = parser.parse(text, VoiceStage.HOME)
            if (parsed.intent != Intent.SAVE || (parsed.cues.category == null && parsed.cues.nameHint == null)) return@launch
            val located = Locator.locate(snap, parsed.cues.time, lookback) ?: return@launch
            val query = queryFor(located, parsed.cues)
            prefetchUsed = true
            prefetch = query to scope.async { runner.run(PIPE, search, query) }
        }
    }

    fun clearPrefetch() {
        debounce?.cancel()
        prefetch?.second?.cancel()
        prefetch = null
        prefetchUsed = false
        window = null
    }

    suspend fun run(snap: LocationSnapshot, parsed: ParsedUtterance, lookbackSec: Int, draft: Place, io: ConfirmIOHost): PersistKind? {
        debounce?.cancel()
        val cues = parsed.cues
        val current = snap.current ?: return PersistKind.Unconfirmed(null, emptyList())
        io.show(SaveFlowState.Searching(parsed.text, null, current.point))
        val located = (runner.run(PIPE, locate, LocateInput(snap, cues.time, lookbackSec)) as? StageOutcome.Ok)?.value
            ?: return PersistKind.Unconfirmed(draft.point, emptyList())
        io.show(SaveFlowState.Searching(parsed.text, located.point, current.point))
        val query = queryFor(located, cues)
        val pre = prefetch?.takeIf { it.first == query }?.second
        if (pre == null) prefetch?.second?.cancel()
        val outcome = pre?.await() ?: runner.run(PIPE, search, query)
        val found = ((outcome as? StageOutcome.Ok)?.value as? PoiResult.Found)?.candidates.orEmpty()
        if (found.isEmpty()) return PersistKind.Unconfirmed(located.point, emptyList())
        val ranked = (runner.run(PIPE, rank, RankInput(found, located, cues)) as? StageOutcome.Ok)?.value.orEmpty()
        val top = ranked.firstOrNull() ?: return PersistKind.Unconfirmed(located.point, emptyList())
        if (top.score < Tunables.SCORE_MIN_TOP) return PersistKind.Unconfirmed(located.point, ranked)
        if (scorer.isCertain(ranked, cues)) return confirmed(top, cues, ranked).copy(certain = true)
        val asked = runner.run(PIPE, confirm, ConfirmInput(ranked, cues, snap, io))
        return when (val r = (asked as? StageOutcome.Ok)?.value ?: ConfirmResult.Unconfirmed) {
            is ConfirmResult.Chosen -> confirmed(r.candidate, cues, ranked)
            ConfirmResult.Unconfirmed -> PersistKind.Unconfirmed(located.point, ranked)
            ConfirmResult.Cancelled -> null
        }
    }

    private fun confirmed(c: PoiCandidate, cues: Cues, ranked: List<PoiCandidate>): PersistKind.Confirmed {
        val category = Categories.label(c.category, cues.category)
        return PersistKind.Confirmed(c, category, ranked)
    }

    private fun queryFor(located: LocatedPoint, cues: Cues) = PoiQuery(
        center = located.point,
        radiusM = Tunables.POI_RADIUS_M,
        category = Categories.byName(cues.category),
        keyword = cues.nameHint,
    )

    private companion object {
        const val PIPE = "save"
    }
}
