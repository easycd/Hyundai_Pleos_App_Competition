package com.easycd.nextplace.feature.visit

import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.LocationQuality
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.model.Visit
import com.easycd.nextplace.core.domain.model.VisitSession
import com.easycd.nextplace.core.domain.model.VisitState
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.repository.VisitRepository
import com.easycd.nextplace.core.domain.repository.VisitSessionRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.feature.visit.note.AttachNoteToRecentVisit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.lang.reflect.Proxy

/** 기술서 7-4 4: 홈 "여기 괜찮았어"는 최근 3시간 안에 떠났거나 지금 머무는 방문에만 붙는다. */
class AttachNoteToRecentVisitTest {
    private val hour = 60 * 60_000L
    private val now = 100 * 24 * hour

    private class Visits(vararg v: Visit) : VisitRepository {
        val rows = v.associateBy { it.id }.toMutableMap()
        override suspend fun insert(visit: Visit) { rows[visit.id] = visit }
        override suspend fun get(id: String) = rows[id]
        override suspend fun setLeftAt(id: String, at: Long) { rows[id] = rows.getValue(id).copy(leftAt = at) }
        override suspend fun setNote(id: String, note: String) { rows[id] = rows.getValue(id).copy(note = note) }
        override suspend fun latestSince(since: Long) =
            rows.values.filter { it.arrivedAt >= since || (it.leftAt ?: Long.MIN_VALUE) >= since }.maxByOrNull { it.arrivedAt }
        override suspend fun lastFor(placeId: String) = rows.values.filter { it.placeId == placeId }.maxByOrNull { it.arrivedAt }
        override suspend fun countFor(placeId: String) = rows.values.count { it.placeId == placeId }
    }

    private class Sessions(private val s: VisitSession?) : VisitSessionRepository {
        override fun observe(): Flow<VisitSession?> = flowOf(s)
        override suspend fun get() = s
        override suspend fun save(session: VisitSession) {}
        override suspend fun clear() {}
    }

    private val places = Proxy.newProxyInstance(PlaceRepository::class.java.classLoader, arrayOf(PlaceRepository::class.java)) { _, m, args ->
        check(m.name == "get") { m.name }
        place(args!![0] as String)
    } as PlaceRepository

    private val clock = object : Clock {
        override fun now() = now
        override fun hourOfDay(atMs: Long) = 12
        override fun dayIndex(atMs: Long) = atMs / (24 * hour)
    }

    private fun place(id: String) = Place(
        id, "가게$id", null, null, 37.5, 127.0, LocationQuality.POI, null, null, PlaceStatus.CONFIRMED, PlaceSource.VOICE,
        false, null, Cues(), null, 0, null, null, null, emptyList(), true, null, null,
    )

    @Test fun oldVisitThatNeverLeftDoesNotTakeTheNote() = runBlocking {
        // 사흘 전 방문: 그 뒤 다른 가게로 안내를 맡겨 판정 세션이 사라져 떠난 시각이 비어 있음
        val visits = Visits(Visit("old", "1", now - 72 * hour, leftAt = null, verified = true, note = null))
        val name = AttachNoteToRecentVisit(visits, Sessions(null), places, clock).attachNoteToRecentVisit("괜찮았어")
        assertNull(name)
        assertNull(visits.rows.getValue("old").note)
    }

    @Test fun visitBeingStayedNowTakesTheNote() = runBlocking {
        val visits = Visits(Visit("stay", "2", now - 4 * hour, leftAt = null, verified = true, note = null))
        val session = VisitSession("2", VisitState.VISITED, now - 5 * hour, now, visitId = "stay")
        val name = AttachNoteToRecentVisit(visits, Sessions(session), places, clock).attachNoteToRecentVisit("맛있었어")
        assertEquals("가게2", name)
        assertEquals("맛있었어", visits.rows.getValue("stay").note)
    }

    @Test fun visitLeftWithinThreeHoursTakesTheNote() = runBlocking {
        val visits = Visits(Visit("left", "3", now - 5 * hour, leftAt = now - 2 * hour, verified = true, note = null))
        val name = AttachNoteToRecentVisit(visits, Sessions(null), places, clock).attachNoteToRecentVisit("별로였어")
        assertEquals("가게3", name)
    }
}
