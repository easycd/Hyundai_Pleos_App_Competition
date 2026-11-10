package com.easycd.nextplace.core.domain.location

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.model.LocationSample

/**
 * 고정 크기 원형 버퍼 (기술서 8장 위치 버퍼, F-02).
 * 객체 생성 없이 배열 5개에 덮어쓰고, 범위는 칸 수가 아니라 시각(최근 60초)으로 자른다.
 * 시각이 직전 칸 이하인 표본(중복·역순)은 버린다. 동시 접근은 synchronized 로 막는다.
 */
class LocationBuffer(private val capacity: Int = Tunables.BUFFER_CAPACITY) {
    private val times = LongArray(capacity)
    private val lats = DoubleArray(capacity)
    private val lons = DoubleArray(capacity)
    private val headings = FloatArray(capacity)
    private val confidences = FloatArray(capacity)
    private var head = 0
    private var size = 0

    /** 표본을 넣는다. 버려지면 false. */
    @Synchronized
    fun add(sample: LocationSample): Boolean {
        if (size > 0 && sample.timeMs <= times[(head - 1 + capacity) % capacity]) return false
        times[head] = sample.timeMs
        lats[head] = sample.lat
        lons[head] = sample.lon
        headings[head] = sample.heading
        confidences[head] = sample.confidence
        head = (head + 1) % capacity
        if (size < capacity) size++
        return true
    }

    @Synchronized
    fun latest(): LocationSample? = if (size == 0) null else read((head - 1 + capacity) % capacity)

    /** 마지막 칸 기준 [windowMs] 안의 표본을 오래된 순으로 복사한다 (불변 스냅샷). */
    @Synchronized
    fun snapshot(nowMs: Long, windowMs: Long = Tunables.BUFFER_WINDOW_MS): List<LocationSample> {
        val out = ArrayList<LocationSample>(size)
        for (i in 0 until size) {
            val idx = (head - size + i + capacity) % capacity
            if (times[idx] >= nowMs - windowMs) out.add(read(idx))
        }
        return out
    }

    @Synchronized
    fun clear() {
        head = 0
        size = 0
    }

    private fun read(idx: Int) = LocationSample(times[idx], lats[idx], lons[idx], headings[idx], confidences[idx])
}
