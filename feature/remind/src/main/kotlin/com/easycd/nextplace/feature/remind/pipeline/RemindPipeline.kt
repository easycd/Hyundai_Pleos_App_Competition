package com.easycd.nextplace.feature.remind.pipeline

import android.content.Context
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Messages
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.contract.PermissionKind
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.domain.pipeline.ListenOutcome
import com.easycd.nextplace.core.domain.pipeline.ListenRequest
import com.easycd.nextplace.core.domain.pipeline.ListenStage
import com.easycd.nextplace.core.domain.pipeline.TraceLog
import com.easycd.nextplace.core.domain.repository.SettingsRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.PermissionChecker
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.usecase.RouteStarter
import com.easycd.nextplace.core.domain.voice.Intent
import com.easycd.nextplace.core.domain.voice.IntentParser
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.core.domain.voice.VoicePrompt.SavedWhen
import com.easycd.nextplace.core.domain.voice.VoiceSession
import com.easycd.nextplace.core.domain.voice.VoiceStage
import com.easycd.nextplace.core.ui.notify.HeadsUp
import com.easycd.nextplace.core.ui.text.TimeText
import com.easycd.nextplace.feature.remind.R
import com.easycd.nextplace.feature.remind.geofence.ProximityEvent
import com.easycd.nextplace.feature.remind.geofence.ProximityWatcher
import com.easycd.nextplace.feature.remind.geofence.RemindGate
import com.easycd.nextplace.feature.remind.pipeline.filter.CooldownFilter
import com.easycd.nextplace.feature.remind.pipeline.filter.DailyOnceFilter
import com.easycd.nextplace.feature.remind.pipeline.filter.MealTimeFilter
import com.easycd.nextplace.feature.remind.pipeline.filter.NaviBusyFilter
import com.easycd.nextplace.feature.remind.pipeline.filter.RemindContext
import com.easycd.nextplace.feature.remind.pipeline.filter.RemindFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 근처 리마인드 (기술서 7-2, F-08): 근접 이벤트 → 필터 4개(순서대로) → 가장 가까운 1곳 → 빈도 문(RemindGate)
 * → 음성 줄(30초 넘으면 생략) → TTS "…300m 앞이에요. 갈까요?" → 음성 5초 · 버튼 10초 → 응이면 내비 목적지 (S-05b).
 */
@Singleton
class RemindPipeline @Inject constructor(
    @ApplicationContext private val context: Context,
    meal: MealTimeFilter,
    daily: DailyOnceFilter,
    naviBusy: NaviBusyFilter,
    cooldown: CooldownFilter,
    private val watcher: ProximityWatcher,
    private val gate: RemindGate,
    private val settings: SettingsRepository,
    private val voice: VoiceSession,
    private val tts: SpeechSynthesizer,
    private val listen: ListenStage,
    private val parser: IntentParser,
    private val routes: RouteStarter,
    private val permissions: PermissionChecker,
    private val headsUp: HeadsUp,
    private val messages: Messages,
    private val navigator: Navigator,
    private val clock: Clock,
    private val trace: TraceLog,
) {
    private val filters: List<RemindFilter> = listOf(meal, daily, naviBusy, cooldown)
    private val _state = MutableStateFlow<RemindState>(RemindState.Hidden)
    val state: StateFlow<RemindState> = _state.asStateFlow()
    private var button: CompletableDeferred<Boolean>? = null

    fun press(go: Boolean) {
        button?.complete(go)
    }

    /** 같은 위치 표본에서 나온 이벤트 묶음. 통과한 것 중 가장 가까운 1곳만. */
    suspend fun handle(events: List<ProximityEvent>) {
        if (events.isEmpty()) return
        val ctx = RemindContext(clock.now(), settings.current())
        val passed = events.filter { e -> passes(e, ctx) }
        val e = passed.minByOrNull { it.distanceM } ?: return
        voice.runQueued(VoiceSession.Kind.REMIND, Tunables.REMIND_QUEUE_MAX_WAIT_MS) {
            if (gate.tryAcquire(e.place.id)) ask(e)
        } ?: trace.event(TAG, "skipped: voice busy over 30s")
    }

    private suspend fun passes(e: ProximityEvent, ctx: RemindContext): Boolean {
        for (f in filters) {
            if (!f.pass(e, ctx)) {
                trace.event(TAG, "filtered by ${f.name}: ${e.place.id}")
                if (f is MealTimeFilter) watcher.hold(e.place.id)
                return false
            }
        }
        return true
    }

    private suspend fun ask(e: ProximityEvent) {
        val p = e.place
        val name = p.name ?: return
        val whenSaved = savedWhen(p)
        val dist = e.distanceM.toInt()
        val asking = RemindState.Asking(name, whenSaved, dist, e.ahead, e.side, null)
        _state.value = asking
        headsUp.show(HeadsUp.REMIND_ID, name, context.getString(R.string.remind_notify_text, distanceLabel(dist, e.ahead)), Tunables.REMIND_BUTTON_WAIT_MS * 2)
        try {
            button = CompletableDeferred()
            tts.speak(VoicePrompt.RemindAsk(name, whenSaved, dist, e.ahead))
            _state.value = asking.copy(waitStartedAt = clock.now())
            if (awaitYes()) route(p, name)
        } finally {
            button = null
            headsUp.cancel(HeadsUp.REMIND_ID)
            _state.value = RemindState.Hidden
        }
    }

    /** 먼저 들어온 대답 하나만: 음성은 5초, 버튼은 10초. */
    private suspend fun awaitYes(): Boolean = coroutineScope {
        val pressed = button!!
        val spoken = async {
            val out = listen.run(ListenRequest(noSpeechTimeoutMs = Tunables.REMIND_VOICE_WAIT_MS))
            (out as? ListenOutcome.Heard)?.let { parser.parse(it.text, VoiceStage.REMIND).intent }
        }
        val first = select<Any?> {
            spoken.onAwait { it }
            pressed.onAwait { it }
        }
        when (first) {
            is Boolean -> {
                spoken.cancel()
                first
            }
            Intent.YES -> true
            Intent.NO -> false
            else -> withTimeoutOrNull(Tunables.REMIND_BUTTON_WAIT_MS - Tunables.REMIND_VOICE_WAIT_MS) { pressed.await() } ?: false
        }
    }

    private suspend fun route(p: Place, name: String) {
        // 내비 권한이 없으면 S-17 '내비를 쓸 수 없어요' (화면 설계서 S-17 들어오는 길: 안내 시작)
        if (!permissions.hasNavi()) return navigator.go(Destination.PermissionNeeded(PermissionKind.NAVI))
        val changing = routes.hasDestination()
        _state.value = RemindState.Routing(name, changing)
        tts.speak(if (changing) VoicePrompt.RouteChange(name) else VoicePrompt.RouteStart(name))
        val ok = routes.send(p)
        if (!ok) {
            messages.show(UserMessage.RouteFailed)
            tts.speak(VoicePrompt.RouteFailed)
        }
        delay(Tunables.RECOMMEND_SEND_DELAY_MS)
    }

    private fun distanceLabel(m: Int, ahead: Boolean) =
        if (ahead) context.getString(R.string.remind_ahead, "${m}m") else context.getString(R.string.remind_near)

    private fun savedWhen(p: Place): SavedWhen {
        if (p.source == PlaceSource.SHARED) return SavedWhen.SHARED
        val days = TimeText.dayDiff(p.savedAt, clock.now())
        return when {
            days <= 0 -> SavedWhen.TODAY
            days == 1 -> SavedWhen.YESTERDAY
            days < WEEK -> SavedWhen.THIS_WEEK
            days < WEEK * 2 -> SavedWhen.LAST_WEEK
            days < MONTH -> SavedWhen.THIS_MONTH
            else -> SavedWhen.LONG_AGO
        }
    }

    private companion object {
        const val TAG = "remind"
        const val WEEK = 7
        const val MONTH = 31
    }
}
