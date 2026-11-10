package com.easycd.nextplace.core.poi

import android.content.Context
import com.easycd.nextplace.core.domain.voice.TextNormalizer
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.poi.PoiQuery
import com.easycd.nextplace.core.domain.poi.PoiResult
import com.easycd.nextplace.core.domain.poi.PoiSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 내장 샘플 가게 (기획서 10장 9번: 인터넷이 막히면 POI 는 앱에 내장한 샘플로 시연).
 * 강남대로 가짜 경로 주변의 가상 가게 32곳. 카카오 키가 없을 때 쓴다.
 */
@Singleton
class SamplePoiSource @Inject constructor(@ApplicationContext private val context: Context) : PoiSource {
    private val places: List<PoiCandidate> by lazy {
        runCatching {
            val text = context.assets.open(FILE).bufferedReader().use { it.readText() }
            Json { ignoreUnknownKeys = true }.decodeFromString(SampleFile.serializer(), text).places.map {
                PoiCandidate(PROVIDER, it.id, it.name, it.category, it.address, it.lat, it.lon)
            }
        }.getOrDefault(emptyList())
    }

    override suspend fun search(query: PoiQuery): PoiResult = PoiResult.Found(places.matching(query).refine(query))

    override suspend fun searchAnywhere(keyword: String): PoiResult {
        val key = TextNormalizer.compact(keyword)
        return PoiResult.Found(places.filter { key.isNotEmpty() && TextNormalizer.compact(it.name).contains(key) }.map { it.copy(distanceM = Double.NaN) })
    }

    @Serializable
    private data class SampleFile(val places: List<SamplePlace>)

    @Serializable
    private data class SamplePlace(val id: String, val name: String, val category: String, val address: String, val lat: Double, val lon: Double)

    companion object {
        const val PROVIDER = "sample"
        private const val FILE = "sample_poi.json"
    }
}
