package com.easycd.nextplace.feature.recommend.session

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Messages
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.contract.PermissionKind
import com.easycd.nextplace.core.domain.contract.RecommendRunner
import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.PermissionChecker
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.usecase.RouteStarter
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.core.domain.voice.VoicePrompt.NoneReason
import com.easycd.nextplace.core.domain.voice.VoiceSession
import com.easycd.nextplace.feature.recommend.score.RecommendScorer
import com.easycd.nextplace.feature.recommend.score.Scored
import com.easycd.nextplace.feature.recommend.score.rule.RecommendContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 추천 세션 (기술서 7-3, RecommendRunner 구현). 말한 곳 기준 10km(넓히면 30km) 안의 확인된 가게를 점수순으로
 * 3장씩 보여 주고(최대 3쪽), 음성·터치 중 먼저 온 선택 하나만 받는다(SelectionGate).
 * 선택 뒤 1.5초 안에 뒤로 가면 아무것도 보내지 않는다. 2분 동안 고르지 않으면 닫는다.
 */
@Singleton
class RecommendSession @Inject constructor(
    private val places: PlaceRepository,
    private val scorer: RecommendScorer,
    private val dialogue: RecommendDialogue,
    private val routes: RouteStarter,
    private val tts: SpeechSynthesizer,
    private val voice: VoiceSession,
    private val permissions: PermissionChecker,
    private val navigator: Navigator,
    private val messages: Messages,
    private val clock: Clock,
) : RecommendRunner {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow<RecommendState>(RecommendState.Closed)
    val state: StateFlow<RecommendState> = _state.asStateFlow()

    private var snapshot: LocationSnapshot? = null
    private var category: String? = null
    private var ranked: List<Scored> = emptyList()
    private var talk: Job? = null
    private var idle: Job? = null
    private var sending: Job? = null
    private val gate = AtomicBoolean(false)
    /** 1.5초가 지나 목적지를 보내기 시작함: 이 뒤로는 앱에서 취소하지 않는다 (안내 취소는 Pleos 내비에서). */
    @Volatile private var committed = false

    override fun open(category: String?, snapshot: LocationSnapshot) {
        this.snapshot = snapshot
        this.category = category
        _state.value = RecommendState.Loading
        navigator.go(Destination.Recommend)
        scope.launch { load(Tunables.RECOMMEND_RADIUS_M) }
    }

    fun widen() {
        scope.launch { load(Tunables.RECOMMEND_WIDE_RADIUS_M) }
    }

    /** 다른 곳 추천: 4~6위, 7~9위 (최대 2회). 더 없으면 "더 없어요"라고 말하고 지금 카드를 둔다. */
    fun next() {
        val s = _state.value as? RecommendState.Showing ?: return
        val nextPage = s.page + 1
        if (nextPage >= Tunables.RECOMMEND_MAX_PAGES || nextPage * Tunables.RECOMMEND_PAGE_SIZE >= ranked.size) {
            messages.show(UserMessage.NoMore)
            scope.launch { tts.speak(VoicePrompt.NoMore) }
            return
        }
        show(nextPage)
    }

    /** 카드 선택 (터치 · 번호 · 이름). 먼저 들어온 하나만. */
    fun select(index: Int) {
        val s = _state.value as? RecommendState.Showing ?: return
        val card = s.cards.getOrNull(index) ?: return
        if (!gate.compareAndSet(false, true)) return
        if (!permissions.hasNavi()) {
            gate.set(false)
            return navigator.go(Destination.PermissionNeeded(PermissionKind.NAVI))
        }
        talk?.cancel()
        idle?.cancel()
        _state.value = s.copy(selectedIndex = index, readingIndex = null)
        committed = false
        sending = scope.launch {
            val place = places.get(card.id) ?: return@launch
            val changing = routes.hasDestination()
            messages.show(UserMessage.Routing(card.name, changing))
            launch { tts.speak(if (changing) VoicePrompt.RouteChange(card.name) else VoicePrompt.RouteStart(card.name)) }
            delay(Tunables.RECOMMEND_SEND_DELAY_MS)
            committed = true
            if (!routes.send(place)) {
                messages.show(UserMessage.RouteFailed)
                tts.speak(VoicePrompt.RouteFailed)
            }
            close()
        }
    }

    /** 뒤로: 1.5초 안이면 선택을 취소하고 S-06 으로, 아니면 홈. */
    fun back() {
        val s = _state.value as? RecommendState.Showing
        if (s?.selectedIndex != null && sending?.isActive == true && !committed) {
            sending?.cancel()
            tts.stop()
            gate.set(false)
            _state.value = s.copy(selectedIndex = null)
            restartIdle()
            return
        }
        close()
    }

    fun close() {
        talk?.cancel()
        idle?.cancel()
        _state.value = RecommendState.Closed
        navigator.go(Destination.Home)
    }

    private suspend fun load(radiusM: Int) {
        val origin = snapshot?.current?.point
        val all = places.allConfirmed()
        val within = if (origin == null) all else places.nearbyConfirmed(origin, radiusM.toDouble())
        ranked = scorer.rank(within, category, RecommendContext(origin, radiusM, clock.now()))
        if (ranked.isEmpty()) {
            val reason = when {
                all.isEmpty() -> NoneReason.NONE_SAVED
                radiusM < Tunables.RECOMMEND_WIDE_RADIUS_M -> NoneReason.NONE_10KM
                else -> NoneReason.NONE_30KM
            }
            _state.value = RecommendState.Empty(category, reason)
            talk = scope.launch { voice.runUser { tts.speak(VoicePrompt.RecommendNone(category, reason)) } }
            return
        }
        show(0)
    }

    private fun show(page: Int) {
        val from = page * Tunables.RECOMMEND_PAGE_SIZE
        val cards = ranked.drop(from).take(Tunables.RECOMMEND_PAGE_SIZE).mapIndexed { i, s ->
            val p = s.item.place
            RecCard(p.id, i + 1, p.name ?: p.displayName ?: "", p.category, s.distanceM, p.memo ?: s.item.visitNotes.firstOrNull())
        }
        gate.set(false)
        _state.value = RecommendState.Showing(category, cards, page)
        talk?.cancel()
        talk = scope.launch {
            voice.runUser {
                dialogue.run(cards, onReading = { i -> update { it.copy(readingIndex = i) } }, onSelect = ::select, onNext = ::next, onCancel = ::close)
            }
        }
        restartIdle()
    }

    private fun restartIdle() {
        idle?.cancel()
        idle = scope.launch {
            delay(Tunables.RECOMMEND_IDLE_CLOSE_MS)
            if ((_state.value as? RecommendState.Showing)?.selectedIndex == null) close()
        }
    }

    private fun update(f: (RecommendState.Showing) -> RecommendState.Showing) {
        val s = _state.value as? RecommendState.Showing ?: return
        _state.value = f(s)
    }
}
