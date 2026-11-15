package com.easycd.nextplace.core.poi

import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.poi.PoiQuery
import com.easycd.nextplace.core.domain.poi.CachedPoi
import com.easycd.nextplace.core.domain.poi.PoiCacheStore
import com.easycd.nextplace.core.domain.poi.PoiResult
import com.easycd.nextplace.core.domain.sdk.Clock
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/** 전수점검 보고서(docs/05) 재현 테스트: B-08. */
class AuditPoiTest {
    @Test fun equivalentKeywordResponseDoesNotDisappearWhenCached() = runBlocking {
        val api = object : KakaoLocalApi {
            override suspend fun category(group: String, lon: Double, lat: Double, radius: Int, sort: String, size: Int) = KakaoSearchResponse()
            override suspend fun keyword(query: String, lon: Double, lat: Double, radius: Int, group: String?, sort: String, size: Int) =
                KakaoSearchResponse(listOf(KakaoPlace("1", "스타벅스 강남역점", "카페", "서울 강남구", null, "127.0", "37.5")))
            override suspend fun keywordAnywhere(query: String, sort: String, size: Int) = KakaoSearchResponse()
            override suspend fun coord2address(lon: Double, lat: Double) = KakaoAddressResponse()
            override suspend fun ping(): retrofit2.Response<Void> = retrofit2.Response.success(null)
        }
        val store = object : PoiCacheStore {
            val rows = mutableMapOf<String, CachedPoi>()
            override suspend fun get(key: String) = rows[key]
            override suspend fun put(key: String, json: String, savedAt: Long) { rows[key] = CachedPoi(json, savedAt) }
            override suspend fun clear() { rows.clear() }
        }
        val clock = object : Clock {
            override fun now() = 1_000_000L
            override fun hourOfDay(atMs: Long) = 12
            override fun dayIndex(atMs: Long) = 0L
        }
        val source = KakaoPoiSource(api, PoiCache(store, clock))
        // 카카오 키워드 검색은 이름뿐 아니라 주소 낱말로도 찾아 준다
        val q = PoiQuery(GeoPoint(37.5, 127.0), 2_000, keyword = "강남 스타벅스")
        val first = source.search(q) as PoiResult.Found
        val cached = source.search(q) as PoiResult.Found
        assertEquals(1, first.candidates.size)
        assertEquals("The same result must remain on a repeated search", first.candidates, cached.candidates)
    }
}
