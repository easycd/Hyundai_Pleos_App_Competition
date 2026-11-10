package com.easycd.nextplace.core.ui.text

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** "오늘 18:42" · "어제 18:42" · "11월 3일" (화면 설계서 S-01 최근 저장). */
object TimeText {
    fun saved(atMs: Long, nowMs: Long = System.currentTimeMillis()): String {
        val days = dayDiff(atMs, nowMs)
        val hm = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(atMs))
        return when (days) {
            0 -> "오늘 $hm"
            1 -> "어제 $hm"
            else -> SimpleDateFormat("M월 d일", Locale.KOREA).format(Date(atMs))
        }
    }

    fun monthDay(atMs: Long): String = SimpleDateFormat("M월 d일", Locale.KOREA).format(Date(atMs))

    fun date(atMs: Long): String = SimpleDateFormat("yyyy.M.d", Locale.KOREA).format(Date(atMs))

    fun dayDiff(atMs: Long, nowMs: Long): Int {
        val a = Calendar.getInstance().apply { timeInMillis = atMs; zero() }
        val b = Calendar.getInstance().apply { timeInMillis = nowMs; zero() }
        return ((b.timeInMillis - a.timeInMillis) / DAY_MS).toInt()
    }

    private fun Calendar.zero() {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    private const val DAY_MS = 86_400_000L
}

/** 거리 "120m" · "1.2km". */
object DistanceText {
    fun of(m: Double): String = if (m < KM) "${m.toInt()}m" else String.format(Locale.US, "%.1fkm", m / KM)
    private const val KM = 1_000.0
}

/** 진행 방향 → "북쪽" 등 8방위. */
object HeadingText {
    private val names = listOf("북쪽", "북동쪽", "동쪽", "남동쪽", "남쪽", "남서쪽", "서쪽", "북서쪽")
    fun of(deg: Double): String = names[(((deg % 360 + 360) % 360 + 22.5) / 45).toInt() % 8]
}
