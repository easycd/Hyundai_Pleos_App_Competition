package com.easycd.nextplace.core.pleos.location

import android.content.Context
import com.easycd.nextplace.core.domain.model.LocationSample
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 가짜 위치 (기획서 12장): docs/tools/route_gangnam_60s.json (강남대로 북쪽 방향 약 1km, 60초, 1Hz)을
 * 1초에 1점씩 되풀이 재생한다. 시각은 지금 시각으로 바꿔 넣는다. 화면 구석에 '시연용 가짜 위치'로 표시된다.
 */
@Singleton
class FakeLocationFeed @Inject constructor(@ApplicationContext private val context: Context) : LocationFeed {
    override val status: StateFlow<SdkStatus> = MutableStateFlow(SdkStatus.READY)
    override val simulated = true

    private val route: List<RoutePoint> by lazy {
        runCatching {
            val text = context.assets.open(ROUTE_FILE).bufferedReader().use { it.readText() }
            Json { ignoreUnknownKeys = true }.decodeFromString(RouteFile.serializer(), text).samples
        }.getOrDefault(listOf(RoutePoint(0, DEFAULT_LAT, DEFAULT_LON, 0.0)))
    }

    override fun updates(): Flow<LocationSample> = flow {
        var i = 0
        while (true) {
            val p = route[i % route.size]
            emit(LocationSample(System.currentTimeMillis(), p.lat, p.lon, p.bearing.toFloat(), CONFIDENCE))
            i++
            delay(PERIOD_MS)
        }
    }

    @Serializable
    private data class RouteFile(val samples: List<RoutePoint>)

    @Serializable
    private data class RoutePoint(val t: Long, val lat: Double, val lon: Double, val bearing: Double)

    private companion object {
        const val ROUTE_FILE = "route_gangnam_60s.json"
        const val PERIOD_MS = 1_000L
        const val CONFIDENCE = 0.9f
        const val DEFAULT_LAT = 37.4979
        const val DEFAULT_LON = 127.0276
    }
}
