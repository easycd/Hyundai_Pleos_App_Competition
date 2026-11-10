package com.easycd.nextplace.core.poi

import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.poi.PoiQuery
import com.easycd.nextplace.core.domain.poi.PoiResult
import com.easycd.nextplace.core.domain.poi.PoiSource
import com.easycd.nextplace.core.domain.voice.Category
import com.easycd.nextplace.core.domain.voice.TextNormalizer
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

/**
 * 카카오 로컬 API 가게 찾기 (F-04, F-07). 캐시 먼저, 없으면 카카오.
 * 캐시 열쇠 = 엔드포인트·정규화 검색어·업종 코드·반경·100m 칸. 칸 중심에서 반경 + 100m 로 넓게 받아 두고
 * 꺼낼 때 실제 기준점에서 반경으로 다시 거른다 (기술서 9장).
 */
internal class KakaoPoiSource @Inject constructor(
    private val api: KakaoLocalApi,
    private val cache: PoiCache,
) : PoiSource {
    private val json = Json { ignoreUnknownKeys = true }
    private val listSerializer = ListSerializer(CachedCandidate.serializer())

    override suspend fun search(query: PoiQuery): PoiResult {
        val keyword = query.keyword?.takeIf { it.isNotBlank() }
        val group = query.category?.kakaoGroup ?: Category.FOOD
        val endpoint = if (keyword != null || query.category != null) "keyword" else "category"
        val text = keyword ?: query.category?.name ?: ""
        val cell = GeoPoint(round3(query.center.lat), round3(query.center.lon))
        val wideRadius = query.radiusM + CELL_M
        val key = listOf(endpoint, TextNormalizer.compact(text), if (keyword != null) "" else group, wideRadius, cell.lat, cell.lon)
            .joinToString("|")
        cache.fresh(key)?.let { return PoiResult.Found(decode(it).refine(query).matching(query)) }
        return try {
            val docs = if (endpoint == "category") {
                api.category(group, cell.lon, cell.lat, wideRadius.coerceAtMost(MAX_RADIUS)).documents
            } else {
                api.keyword(text, cell.lon, cell.lat, wideRadius.coerceAtMost(MAX_RADIUS), if (keyword != null) null else group).documents
            }
            val list = docs.mapNotNull { d ->
                val lat = d.y.toDoubleOrNull() ?: return@mapNotNull null
                val lon = d.x.toDoubleOrNull() ?: return@mapNotNull null
                PoiCandidate(PROVIDER, d.id, d.name, d.category, d.roadAddress?.takeIf { it.isNotBlank() } ?: d.address, lat, lon)
            }
            cache.put(key, encode(list))
            PoiResult.Found(list.refine(query))
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            cache.staleOk(key)?.let { PoiResult.Found(decode(it).refine(query).matching(query), stale = true) } ?: PoiResult.Offline
        } catch (e: Exception) {
            PoiResult.Failed
        }
    }

    private fun encode(list: List<PoiCandidate>) = json.encodeToString(listSerializer, list.map(CachedCandidate::from))
    private fun decode(s: String) = runCatching { json.decodeFromString(listSerializer, s).map { it.toModel() } }.getOrDefault(emptyList())

    private fun round3(v: Double) = String.format(Locale.US, "%.3f", v).toDouble()

    companion object {
        const val PROVIDER = "kakao"
        private const val CELL_M = 100
        private const val MAX_RADIUS = 20_000
    }

    @kotlinx.serialization.Serializable
    private data class CachedCandidate(
        val id: String, val name: String, val category: String? = null, val address: String? = null,
        val lat: Double, val lon: Double,
    ) {
        fun toModel() = PoiCandidate(PROVIDER, id, name, category, address, lat, lon)
        companion object {
            fun from(c: PoiCandidate) = CachedCandidate(c.providerId, c.name, c.category, c.address, c.lat, c.lon)
        }
    }
}
