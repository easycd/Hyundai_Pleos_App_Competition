package com.easycd.nextplace.feature.voicesave.pipeline

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Messages
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.contract.PermissionKind
import com.easycd.nextplace.core.domain.contract.RecommendRunner
import com.easycd.nextplace.core.domain.contract.SaveRunner
import com.easycd.nextplace.core.domain.contract.VisitNoteRunner
import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.domain.pipeline.CaptureStage
import com.easycd.nextplace.core.domain.pipeline.ListenOutcome
import com.easycd.nextplace.core.domain.pipeline.ListenRequest
import com.easycd.nextplace.core.domain.pipeline.ListenStage
import com.easycd.nextplace.core.domain.pipeline.StageOutcome
import com.easycd.nextplace.core.domain.pipeline.StageRunner
import com.easycd.nextplace.core.domain.repository.SettingsRepository
import com.easycd.nextplace.core.domain.sdk.NaviController
import com.easycd.nextplace.core.domain.sdk.PermissionChecker
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import com.easycd.nextplace.core.domain.sdk.SpeechRecognizer
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.sdk.SttEvent
import com.easycd.nextplace.core.domain.sdk.SttFailure
import com.easycd.nextplace.core.domain.voice.Intent
import com.easycd.nextplace.core.domain.voice.ParsedUtterance
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.core.domain.voice.VoiceSession
import com.easycd.nextplace.feature.voicesave.pipeline.stage.ParseStage
import com.easycd.nextplace.feature.voicesave.pipeline.stage.PersistInput
import com.easycd.nextplace.feature.voicesave.pipeline.stage.PersistKind
import com.easycd.nextplace.feature.voicesave.pipeline.stage.PersistResult
import com.easycd.nextplace.feature.voicesave.pipeline.stage.PersistStage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 음성 저장 파이프라인 (기술서 7-1, SaveRunner 구현). 1 Capture → 2 Listen → 3 Parse → (4~7 [FindAndConfirm]) → 8 Persist.
 * 상태는 [state] 하나로만 나간다 (R6). 어느 단계가 실패하든 미확인 저장으로 떨어지고, '취소'면 초안을 지운다.
 */
@Singleton
class SavePipeline @Inject constructor(
    private val runner: StageRunner,
    private val capture: CaptureStage,
    private val listen: ListenStage,
    private val parse: ParseStage,
    private val find: FindAndConfirm,
    private val persist: PersistStage,
    private val drafts: DraftStore,
    private val session: VoiceSession,
    private val recognizer: SpeechRecognizer,
    private val tts: SpeechSynthesizer,
    private val permissions: PermissionChecker,
    private val settings: SettingsRepository,
    private val navi: NaviController,
    private val navigator: Navigator,
    private val messages: Messages,
    private val recommend: RecommendRunner,
    private val visitNote: VisitNoteRunner,
) : SaveRunner, ConfirmIOHost {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow<SaveFlowState>(SaveFlowState.Idle)
    val state: StateFlow<SaveFlowState> = _state.asStateFlow()
    override val answers = Channel<ConfirmAnswer>(Channel.CONFLATED)

    @Volatile private var job: Job? = null
    @Volatile private var userCancelled = false
    @Volatile private var draft: Place? = null

    override fun show(state: SaveFlowState) {
        _state.value = state
    }

    override fun startVoiceSave() {
        when {
            !permissions.hasMicrophone() -> return navigator.go(Destination.PermissionNeeded(PermissionKind.MICROPHONE))
            recognizer.status.value == SdkStatus.UNAVAILABLE -> return navigator.go(Destination.PermissionNeeded(PermissionKind.VOICE_SERVICE))
        }
        job = scope.launch { session.runUser { runSave() } }
    }

    /** 취소 버튼 · 음성 "취소": 아무것도 저장하지 않고 원래 화면으로. */
    fun cancel() {
        userCancelled = true
        tts.stop()
        job?.cancel()
    }

    fun answer(a: ConfirmAnswer) {
        answers.trySend(a)
    }

    override suspend fun saveLocationOnly() {
        val snap = capture.run(PlaceSource.TOUCH)
        val point = snap.current?.point ?: return
        val d = drafts.create(snap, null) ?: return report(PersistResult.Failed)
        report(persist.run(PersistInput(d, PersistKind.Unconfirmed(point, emptyList(), navi.roadName.value))))
    }

    private suspend fun runSave() {
        userCancelled = false
        draft = null
        try {
            val snap = (runner.run(PIPE, capture, PlaceSource.VOICE) as? StageOutcome.Ok)?.value ?: return
            val lookback = settings.current().lookbackSec
            val text = listenWithRetry(snap, lookback) ?: return
            val parsed = (runner.run(PIPE, parse, text) as? StageOutcome.Ok)?.value ?: ParsedUtterance(text, Intent.SAVE)
            _state.value = SaveFlowState.Understanding(text)
            route(snap, parsed, lookback)
        } catch (e: CancellationException) {
            withContext(NonCancellable) { onCancelled() }
            throw e
        } finally {
            find.clearPrefetch()
            _state.value = SaveFlowState.Idle
        }
    }

    private suspend fun route(snap: LocationSnapshot, parsed: ParsedUtterance, lookback: Int) {
        when (parsed.intent) {
            Intent.CANCEL -> {
                tts.speak(VoicePrompt.Cancelled)
                return
            }
            Intent.RECOMMEND -> {
                _state.value = SaveFlowState.Idle
                recommend.open(parsed.cues.category, snap)
                return
            }
            Intent.NOTE -> {
                val name = visitNote.attachNoteToRecentVisit(parsed.text)
                if (name != null) {
                    messages.show(UserMessage.NoteSaved(name, parsed.text))
                    tts.speak(VoicePrompt.NoteSaved(name))
                    return
                }
            }
            else -> Unit
        }
        val d = drafts.create(snap, parsed.text)
        if (d == null) return report(PersistResult.Failed)
        draft = d.copy(cues = parsed.cues, memo = parsed.cues.memo)
        val kind = find.run(snap, parsed, lookback, draft!!, this) ?: run {
            drafts.delete(d.id)
            draft = null
            tts.speak(VoicePrompt.Cancelled)
            return
        }
        val withRoad = if (kind is PersistKind.Unconfirmed) kind.copy(roadName = navi.roadName.value) else kind
        val result = runner.run(PIPE, persist, PersistInput(draft!!, withRoad))
        draft = null
        report((result as? StageOutcome.Ok)?.value ?: PersistResult.Failed)
    }

    /** 2단계 듣기. 빈 문장·인식 실패면 S-02c 로 1회 다시 듣고, 또 실패면 토스트만 남기고 닫는다. */
    private suspend fun listenWithRetry(snap: LocationSnapshot, lookback: Int): String? {
        repeat(LISTEN_TRIES) { attempt ->
            _state.value = SaveFlowState.Listening("", 0f)
            find.startPrefetchWindow(snap, lookback)
            val out = runner.run(PIPE, listen, ListenRequest(progress = { onSttEvent(it) }))
            when (val o = (out as? StageOutcome.Ok)?.value) {
                is ListenOutcome.Heard -> return o.text
                is ListenOutcome.Failed -> if (o.reason == SttFailure.PERMISSION || o.reason == SttFailure.UNAVAILABLE) {
                    val kind = if (o.reason == SttFailure.PERMISSION) PermissionKind.MICROPHONE else PermissionKind.VOICE_SERVICE
                    navigator.go(Destination.PermissionNeeded(kind))
                    return null
                }
                else -> Unit
            }
            if (attempt == 0) {
                _state.value = SaveFlowState.Retry
                tts.speak(VoicePrompt.SayAgain)
                delay(Tunables.LISTEN_RETRY_DELAY_MS)
            }
        }
        messages.show(UserMessage.SayAgainLater)
        return null
    }

    private fun onSttEvent(e: SttEvent) {
        val cur = _state.value as? SaveFlowState.Listening ?: return
        when (e) {
            is SttEvent.Partial -> {
                _state.value = cur.copy(partial = e.text)
                find.onPartial(e.text)
            }
            is SttEvent.Level -> _state.value = cur.copy(level = e.level)
            else -> Unit
        }
    }

    /** 사용자 취소면 초안을 지우고, 새 말하기 버튼(선점)·포커스 잃음이면 미확인으로 마무리한다. */
    private suspend fun onCancelled() {
        val d = draft ?: return
        draft = null
        if (userCancelled) {
            drafts.delete(d.id)
        } else {
            persist.run(PersistInput(d, PersistKind.Unconfirmed(d.point, emptyList(), navi.roadName.value)))
        }
    }

    private suspend fun report(r: PersistResult) {
        val road = navi.roadName.value
        val (msg, prompt) = when (r) {
            is PersistResult.Saved -> UserMessage.Saved(r.name) to if (r.certain) VoicePrompt.SavedNamed(r.name) else VoicePrompt.Saved
            is PersistResult.Merged -> UserMessage.AlreadySaved(r.name) to VoicePrompt.AlreadySaved
            PersistResult.LocationOnly -> UserMessage.SavedLocationOnly(road) to VoicePrompt.SavedLocationOnly
            PersistResult.TextOnly -> UserMessage.SavedTextOnly to VoicePrompt.SavedTextOnly
            PersistResult.Failed -> UserMessage.SaveFailed to VoicePrompt.SaveFailed
        }
        _state.value = SaveFlowState.Idle
        messages.show(msg)
        tts.speak(prompt)
    }

    private companion object {
        const val PIPE = "save"
        const val LISTEN_TRIES = 2
    }
}

/** 확인 단계가 쓰는 출구: 상태 갱신과 버튼 대답 통로. */
interface ConfirmIOHost : com.easycd.nextplace.feature.voicesave.pipeline.stage.ConfirmIO
