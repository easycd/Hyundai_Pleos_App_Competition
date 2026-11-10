package com.easycd.nextplace.core.poi

import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.poi.ReverseGeocoder
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/** 좌표 → 도로명 (홈 현재 위치). 실패하면 null → 화면은 "현재 위치". */
internal class KakaoReverseGeocoder @Inject constructor(private val api: KakaoLocalApi) : ReverseGeocoder {
    override suspend fun roadName(point: GeoPoint): String? = try {
        val doc = api.coord2address(point.lon, point.lat).documents.firstOrNull()
        doc?.road?.roadName?.takeIf { it.isNotBlank() } ?: doc?.address?.dong
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}

/** 키가 없을 때: 도로명 없음. */
internal class NoReverseGeocoder @Inject constructor() : ReverseGeocoder {
    override suspend fun roadName(point: GeoPoint): String? = null
}
