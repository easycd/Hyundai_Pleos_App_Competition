package com.easycd.nextplace.feature.recommend

import com.easycd.nextplace.core.domain.contract.Messages
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.model.PlaceWithVisits
import com.easycd.nextplace.core.domain.model.VisitSession
import com.easycd.nextplace.core.domain.pipeline.ListenStage
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.repository.VisitSessionRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.NaviController
import com.easycd.nextplace.core.domain.sdk.NaviEvent
import com.easycd.nextplace.core.domain.sdk.PermissionChecker
import com.easycd.nextplace.core.domain.sdk.RouteTarget
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import com.easycd.nextplace.core.domain.sdk.SpeechRecognizer
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.sdk.SttEvent
import com.easycd.nextplace.core.domain.usecase.RouteStarter
import com.easycd.nextplace.core.domain.voice.IntentParser
import com.easycd.nextplace.core.domain.voice.SlotExtractor
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.core.domain.voice.VoiceSession
import com.easycd.nextplace.feature.recommend.score.RecommendScorer
import com.easycd.nextplace.feature.recommend.score.rule.DistanceRule
import com.easycd.nextplace.feature.recommend.score.rule.LastVisitRule
import com.easycd.nextplace.feature.recommend.score.rule.MemoRule
import com.easycd.nextplace.feature.recommend.session.RecommendDialogue
import com.easycd.nextplace.feature.recommend.session.RecommendSession
import com.easycd.nextplace.feature.recommend.session.RecommendState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** 전수점검 보고서(docs/05) 재현 테스트: B-09. */
class AuditRecommendTest {
    @Test fun closingWhileLoadingDoesNotStartANewDialogueLater() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val places = Proxy.newProxyInstance(PlaceRepository::class.java.classLoader, arrayOf(PlaceRepository::class.java)) { _, method, _ ->
            check(method.name == "allConfirmed") { method.name }
            entered.countDown()
            check(release.await(3, TimeUnit.SECONDS))
            emptyList<PlaceWithVisits>()
        } as PlaceRepository
        val clock = object : Clock {
            override fun now() = System.currentTimeMillis()
            override fun hourOfDay(atMs: Long) = 12
            override fun dayIndex(atMs: Long) = 0L
        }
        val tts = object : SpeechSynthesizer {
            override val status = MutableStateFlow(SdkStatus.READY)
            override val speaking = MutableStateFlow(false)
            override suspend fun speak(prompt: VoicePrompt) = true
            override fun stop() {}
            override fun setVolume(volume: Float) {}
        }
        val recognizer = object : SpeechRecognizer {
            override val status = MutableStateFlow(SdkStatus.READY)
            override fun listen(): Flow<SttEvent> = emptyFlow()
        }
        val navi = object : NaviController {
            override val status = MutableStateFlow(SdkStatus.READY)
            override val routeActive = MutableStateFlow(false)
            override val roadName = MutableStateFlow<String?>(null)
            override val events: Flow<NaviEvent> = emptyFlow()
            override suspend fun currentDestination(): RouteTarget? = null
            override suspend fun requestRoute(target: RouteTarget) = true
        }
        val sessions = object : VisitSessionRepository {
            override fun observe(): Flow<VisitSession?> = flowOf(null)
            override suspend fun get(): VisitSession? = null
            override suspend fun save(session: VisitSession) {}
            override suspend fun clear() {}
        }
        val permissions = object : PermissionChecker {
            override fun hasMicrophone() = true
            override fun hasLocation() = true
            override fun hasNavi() = true
            override fun hasNotification() = true
        }
        val session = RecommendSession(
            places, RecommendScorer(DistanceRule(), MemoRule(), LastVisitRule()),
            RecommendDialogue(tts, ListenStage(recognizer, clock), IntentParser(SlotExtractor())),
            RouteStarter(navi, sessions, clock), tts, VoiceSession(), permissions, Navigator(), Messages(), clock,
        )
        session.open(null, LocationSnapshot(1, clock.now(), null, emptyList(), PlaceSource.VOICE))
        assertTrue(entered.await(3, TimeUnit.SECONDS))
        session.close()
        release.countDown()
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
        while (session.state.value == RecommendState.Closed && System.nanoTime() < deadline) Thread.sleep(10)
        try { assertEquals(RecommendState.Closed, session.state.value) } finally { session.close() }
    }
}
