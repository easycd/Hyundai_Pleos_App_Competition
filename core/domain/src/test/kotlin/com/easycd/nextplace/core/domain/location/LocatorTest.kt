package com.easycd.nextplace.core.domain.location

import com.easycd.nextplace.core.domain.model.LocationSample
import com.easycd.nextplace.core.domain.model.LocationSnapshot
import com.easycd.nextplace.core.domain.model.TimeCue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** 기획서 6장 4번: '방금'·단서 없음 = T−N, '아까' = T−45초, '여기' = T. 위치 버퍼는 최근 60초, 역순 표본은 버린다. */
class LocatorTest {
    private val t0 = 1_000_000L

    /** 1초에 1번, 북쪽으로 약 11m씩 (위도 0.0001). */
    private fun buffer(seconds: Int): LocationBuffer = LocationBuffer().apply {
        repeat(seconds + 1) { i -> add(LocationSample(t0 + i * 1_000L, 37.5 + i * 0.0001, 127.0, 0f, 1f)) }
    }

    private fun snapshot(buf: LocationBuffer, pressedAt: Long) =
        LocationSnapshot(1, pressedAt, buf.latest(), buf.snapshot(pressedAt))

    @Test
    fun targetTimes() {
        assertEquals(t0 - 20_000, Locator.targetTime(t0, TimeCue.JUST, 20))
        assertEquals(t0 - 10_000, Locator.targetTime(t0, TimeCue.NONE, 10))
        assertEquals(t0 - 45_000, Locator.targetTime(t0, TimeCue.EARLIER, 20))
        assertEquals(t0, Locator.targetTime(t0, TimeCue.HERE, 20))
    }

    @Test
    fun justUsesLookback() {
        val buf = buffer(60)
        val pressed = t0 + 60_000
        val p = Locator.locate(snapshot(buf, pressed), TimeCue.JUST, 20)!!
        assertEquals(37.5 + 40 * 0.0001, p.point.lat, 1e-7)
        assertFalse(p.timeInexact)
        assertEquals(0.0, p.headingDeg!!, 1.0) // 북쪽
    }

    @Test
    fun interpolatesBetweenSamples() {
        val buf = LocationBuffer().apply {
            add(LocationSample(t0, 37.5, 127.0, 0f, 1f))
            add(LocationSample(t0 + 4_000, 37.5004, 127.0, 0f, 1f))
            add(LocationSample(t0 + 30_000, 37.503, 127.0, 0f, 1f))
        }
        val p = Locator.locate(LocationSnapshot(1, t0 + 22_000, buf.latest(), buf.snapshot(t0 + 22_000)), TimeCue.JUST, 20)!!
        assertEquals(37.5002, p.point.lat, 1e-7)
    }

    /** 가짜 경로가 처음으로 돌아가는 것처럼 위치가 1km 순간이동하면 그 앞의 표본은 쓰지 않는다 (실행 점검 8번: '방금 지난' 후보가 747m 앞). */
    @Test
    fun ignoresSamplesBeforeJump() {
        val buf = LocationBuffer().apply {
            repeat(40) { i -> add(LocationSample(t0 + i * 1_000L, 37.5 + i * 0.0001, 127.0, 0f, 1f)) }
            repeat(20) { i -> add(LocationSample(t0 + (40 + i) * 1_000L, 37.491 + i * 0.0001, 127.0, 0f, 1f)) }
        }
        val pressed = t0 + 59_000
        val p = Locator.locate(LocationSnapshot(1, pressed, buf.latest(), buf.snapshot(pressed)), TimeCue.JUST, 20)!!
        assertEquals(37.491, p.point.lat, 1e-7) // 순간이동 뒤 첫 표본 (말한 곳 뒤쪽), 앞쪽 1km 지점이 아님
        assertEquals(20, Locator.continuousTail(buf.snapshot(pressed)).size)
    }

    @Test
    fun hereUsesCurrent() {
        val buf = buffer(30)
        val p = Locator.locate(snapshot(buf, t0 + 30_000), TimeCue.HERE, 20)!!
        assertEquals(buf.latest()!!.lat, p.point.lat, 1e-9)
    }

    @Test
    fun gapFallsBackToCurrentAndMarksInexact() {
        val buf = LocationBuffer().apply { add(LocationSample(t0 + 59_000, 37.5, 127.0, 0f, 1f)) }
        val p = Locator.locate(LocationSnapshot(1, t0 + 60_000, buf.latest(), buf.snapshot(t0 + 60_000)), TimeCue.EARLIER, 20)!!
        assertTrue(p.timeInexact)
    }

    @Test
    fun noLocation() {
        assertNull(Locator.locate(LocationSnapshot(1, t0, null, emptyList()), TimeCue.JUST, 20))
    }

    @Test
    fun bufferDropsOutOfOrderAndKeepsWindow() {
        val buf = LocationBuffer(capacity = 8)
        assertTrue(buf.add(LocationSample(t0, 1.0, 1.0, 0f, 1f)))
        assertFalse(buf.add(LocationSample(t0, 2.0, 2.0, 0f, 1f)))
        assertFalse(buf.add(LocationSample(t0 - 1, 2.0, 2.0, 0f, 1f)))
        repeat(20) { i -> buf.add(LocationSample(t0 + (i + 1) * 10_000L, 1.0, 1.0, 0f, 1f)) }
        val snap = buf.snapshot(t0 + 200_000, windowMs = 60_000)
        assertEquals(7, snap.size) // 140~200초
        assertTrue(snap.zipWithNext().all { (a, b) -> a.timeMs < b.timeMs })
    }
}
