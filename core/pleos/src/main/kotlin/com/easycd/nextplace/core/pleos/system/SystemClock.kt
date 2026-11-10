package com.easycd.nextplace.core.pleos.system

import com.easycd.nextplace.core.domain.sdk.Clock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/** 차량 시간 기준 시계. 날짜 경계는 0시. */
class SystemClock @Inject constructor() : Clock {
    override fun now(): Long = System.currentTimeMillis()
    override fun hourOfDay(atMs: Long): Int = Instant.ofEpochMilli(atMs).atZone(ZoneId.systemDefault()).hour
    override fun dayIndex(atMs: Long): Long = Instant.ofEpochMilli(atMs).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
}
