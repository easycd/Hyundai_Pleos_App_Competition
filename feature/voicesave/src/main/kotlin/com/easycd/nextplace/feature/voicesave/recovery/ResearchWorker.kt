package com.easycd.nextplace.feature.voicesave.recovery

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.PlaceStatus
import com.easycd.nextplace.core.domain.poi.PoiQuery
import com.easycd.nextplace.core.domain.poi.PoiResult
import com.easycd.nextplace.core.domain.poi.PoiSource
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.voice.Categories
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 후보 없이 저장된 미확인 가게를 다시 찾는다. 찾으면 Place.candidates 를 채우고 '후보 찾음' 표시 (홈 칩 → S-09).
 * 쓰기 직전에 행이 있고, 삭제 예정이 아니고, 아직 미확인인지 다시 확인한다 (기술서 8장). 오프라인이면 다시 시도.
 */
@HiltWorker
class ResearchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val places: PlaceRepository,
    private val poi: PoiSource,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        var offline = false
        for (place in places.unconfirmedWithoutCandidates()) {
            val point = place.point ?: continue
            val query = PoiQuery(point, Tunables.POI_RADIUS_M, category = Categories.byName(place.cues.category), keyword = place.cues.nameHint)
            when (val r = poi.search(query)) {
                is PoiResult.Found -> if (r.candidates.isNotEmpty()) {
                    val now = places.get(place.id)
                    if (now != null && now.deletedAt == null && now.status == PlaceStatus.UNCONFIRMED && now.candidates.isEmpty()) {
                        val sorted = r.candidates.sortedBy { Geo.distanceM(point, GeoPoint(it.lat, it.lon)) }.take(Tunables.KEEP_CANDIDATES)
                        places.update(now.copy(candidates = sorted, candidatesFoundLater = true))
                    }
                }
                PoiResult.Offline -> offline = true
                PoiResult.Failed -> Unit
            }
        }
        return if (offline && runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.success()
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
    }
}
