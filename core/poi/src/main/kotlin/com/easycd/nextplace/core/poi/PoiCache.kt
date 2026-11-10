package com.easycd.nextplace.core.poi

import com.easycd.nextplace.core.domain.poi.PoiCacheStore
import com.easycd.nextplace.core.domain.sdk.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * POI 응답 캐시 (기술서 9장): 메모리 200건 + Room 2,000건. 24시간 안이면 그대로,
 * 오프라인이면 7일까지 '오래된 결과'로 쓴다.
 */
@Singleton
class PoiCache @Inject constructor(private val store: PoiCacheStore, private val clock: Clock) {
    private val memory = object : LinkedHashMap<String, Pair<String, Long>>(MEMORY_MAX, LOAD, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Pair<String, Long>>?) = size > MEMORY_MAX
    }

    suspend fun fresh(key: String): String? = read(key)?.takeIf { clock.now() - it.second <= FRESH_MS }?.first

    suspend fun staleOk(key: String): String? = read(key)?.takeIf { clock.now() - it.second <= STALE_MS }?.first

    suspend fun put(key: String, json: String) {
        val now = clock.now()
        synchronized(memory) { memory[key] = json to now }
        store.put(key, json, now)
    }

    private suspend fun read(key: String): Pair<String, Long>? {
        synchronized(memory) { memory[key] }?.let { return it }
        val hit = store.get(key) ?: return null
        val pair = hit.json to hit.savedAt
        synchronized(memory) { memory[key] = pair }
        return pair
    }

    private companion object {
        const val MEMORY_MAX = 200
        const val LOAD = 0.75f
        const val FRESH_MS = 24 * 60 * 60_000L
        const val STALE_MS = 7 * 24 * 60 * 60_000L
    }
}
