package com.easycd.nextplace.feature.remind

import com.easycd.nextplace.core.domain.model.AppSettings
import com.easycd.nextplace.core.domain.model.Cues
import com.easycd.nextplace.core.domain.model.LocationQuality
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceSource
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.repository.SettingsRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.feature.remind.geofence.RemindGate
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Test
import java.lang.reflect.Proxy

/** 전수점검 보고서(docs/05) 재현 테스트: B-10. */
class AuditRemindTest {
    @Test fun queuedReminderIsRejectedAfterUserDisablesReminders() = runBlocking {
        val p = Place("p", "서울갈비", "고깃집", null, 37.5, 127.0, LocationQuality.POI,
            "kakao", "1", PlaceStatus.CONFIRMED, PlaceSource.VOICE, false, null,
            Cues(), null, 0, null, null, null, emptyList(), true, null, null)
        val places = Proxy.newProxyInstance(PlaceRepository::class.java.classLoader,
            arrayOf(PlaceRepository::class.java)) { _, method, _ ->
            when (method.name) { "get" -> p; "setLastReminded" -> Unit; else -> error(method.name) }
        } as PlaceRepository
        val settings = object : SettingsRepository {
            var value = AppSettings(remindEnabled = false)
            override val settings get() = flowOf(value)
            override suspend fun current() = value
            override suspend fun update(transform: (AppSettings) -> AppSettings) { value = transform(value) }
        }
        val clock = object : Clock {
            override fun now() = 1_000_000L
            override fun hourOfDay(atMs: Long) = 12
            override fun dayIndex(atMs: Long) = 0L
        }
        assertFalse("The final gate must reject a disabled reminder", RemindGate(places, settings, clock).tryAcquire("p"))
    }
}
