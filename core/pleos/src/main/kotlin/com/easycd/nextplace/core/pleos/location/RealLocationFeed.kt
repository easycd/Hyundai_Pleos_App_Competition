package com.easycd.nextplace.core.pleos.location

import ai.pleos.playground.location.fused.FusedLocation
import ai.pleos.playground.location.fused.listener.FusedLocationListener
import ai.pleos.playground.location.fused.model.LocationData
import android.content.Context
import com.easycd.nextplace.core.domain.model.LocationSample
import com.easycd.nextplace.core.domain.sdk.SdkStatus
import com.easycd.nextplace.core.pleos.system.PleosScope
import com.easycd.nextplace.core.pleos.system.connectWithRetry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/** FusedLocation SDK 1.1.3 (위도·경도·진행 방향·신뢰도·시각). 속도 필드는 없다 (기획서 10장 3번). */
@Singleton
class RealLocationFeed @Inject constructor(
    @ApplicationContext private val context: Context,
    scope: PleosScope,
) : LocationFeed {
    private val _status = MutableStateFlow(SdkStatus.CONNECTING)
    override val status: StateFlow<SdkStatus> = _status.asStateFlow()
    override val simulated = false
    private var fused: FusedLocation? = null

    init {
        scope.connectWithRetry(_status) {
            val f = fused ?: FusedLocation(context).also { fused = it }
            f.initialize()
            f.isSupported()
        }
    }

    override fun updates(): Flow<LocationSample> = callbackFlow {
        val f = fused
        if (f == null) {
            close()
            return@callbackFlow
        }
        val listener = object : FusedLocationListener {
            override fun onChanged(data: LocationData?) {
                data?.let { trySend(it.toSample()) }
            }
            override fun onError(e: Exception) = Unit
        }
        f.getLastKnownLocation(object : FusedLocationListener {
            override fun onChanged(data: LocationData?) { data?.let { trySend(it.toSample()) } }
            override fun onError(e: Exception) = Unit
        })
        f.registerFusedLocationCallback(listener)
        awaitClose { runCatching { f.unregisterFusedLocationCallback(listener) } }
    }

    /** SDK 시각이 벽시계와 크게 다르면(부팅 기준 시각 등) 받은 시각으로 바꾼다. */
    private fun LocationData.toSample(): LocationSample {
        val now = System.currentTimeMillis()
        val t = if (abs(timestampMillis - now) < CLOCK_TOLERANCE_MS) timestampMillis else now
        return LocationSample(t, latitude, longitude, heading, confidence)
    }

    private companion object {
        const val CLOCK_TOLERANCE_MS = 60_000L
    }
}
