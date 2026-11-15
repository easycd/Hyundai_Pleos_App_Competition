package com.easycd.nextplace.core.domain

import com.easycd.nextplace.core.domain.contract.RouteNotice
import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.LocationQuality
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.model.VisitSession
import com.easycd.nextplace.core.domain.model.VisitState
import com.easycd.nextplace.core.domain.repository.VisitSessionRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.NaviController
import com.easycd.nextplace.core.domain.sdk.NaviEvent
import com.easycd.nextplace.core.domain.sdk.RouteTarget
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import com.easycd.nextplace.core.domain.usecase.RouteStarter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** 화면 설계서 S-05b: 안내 시작 확인 문구는 내비 화면 위 차량 알림으로 뜨고, 성공하면 방문 판정(ROUTING)이 시작된다. */
class RouteStarterTest {
    private class Navi(private val ok: Boolean) : NaviController {
        val sent = mutableListOf<RouteTarget>()
        override val status = MutableStateFlow(SdkStatus.READY)
        override val routeActive = MutableStateFlow(false)
        override val roadName = MutableStateFlow<String?>(null)
        override val events: Flow<NaviEvent> = emptyFlow()
        override suspend fun currentDestination(): RouteTarget? = null
        override suspend fun requestRoute(target: RouteTarget): Boolean = ok.also { sent += target }
    }

    private class Sessions : VisitSessionRepository {
        var saved: VisitSession? = null
        override fun observe(): Flow<VisitSession?> = flowOf(saved)
        override suspend fun get() = saved
        override suspend fun save(session: VisitSession) { saved = session }
        override suspend fun clear() { saved = null }
    }

    private val clock = object : Clock {
        override fun now() = 1_000L
        override fun hourOfDay(atMs: Long) = 12
        override fun dayIndex(atMs: Long) = 0L
    }

    private val place = Place(
        "p1", "서울갈비", "고깃집", "서울 강남구", 37.5, 127.0, LocationQuality.POI, "kakao", "1", PlaceStatus.CONFIRMED,
        PlaceSource.VOICE, false, null, Cues(), null, 0, null, null, null, emptyList(), true, null, null,
    )

    @Test fun showsNoticeAndStartsVisitSession() = runBlocking {
        val shown = mutableListOf<Pair<String, Boolean>>()
        val sessions = Sessions()
        val navi = Navi(ok = true)
        val ok = RouteStarter(navi, sessions, clock, RouteNotice { n, c -> shown += n to c }).send(place, changing = true)
        assertTrue(ok)
        assertEquals(listOf("서울갈비" to true), shown)
        assertEquals(1, navi.sent.size)
        assertEquals(VisitState.ROUTING, sessions.saved?.state)
    }

    @Test fun retriesOnceThenFailsWithoutVisitSession() = runBlocking {
        val sessions = Sessions()
        val navi = Navi(ok = false)
        val ok = RouteStarter(navi, sessions, clock, RouteNotice { _, _ -> }).send(place)
        assertFalse(ok)
        assertEquals(2, navi.sent.size)
        assertEquals(null, sessions.saved)
    }
}
