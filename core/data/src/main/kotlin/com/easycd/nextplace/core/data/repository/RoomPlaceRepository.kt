package com.easycd.nextplace.core.data.repository

import androidx.room.withTransaction
import com.easycd.nextplace.core.data.db.NextPlaceDb
import com.easycd.nextplace.core.data.mapper.toEntity
import com.easycd.nextplace.core.data.mapper.toModel
import com.easycd.nextplace.core.domain.geo.Geo
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.Place
import com.easycd.nextplace.core.domain.model.PlaceWithVisits
import com.easycd.nextplace.core.domain.repository.PlaceCounts
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.voice.TextNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** 가게 저장소 Room 구현 (기술서 8장). */
internal class RoomPlaceRepository @Inject constructor(private val db: NextPlaceDb) : PlaceRepository {
    private val dao = db.placeDao()
    private val visits = db.visitDao()

    override fun observeAll(): Flow<List<PlaceWithVisits>> = dao.observeAll().map { rows -> rows.map { it.toModel() } }

    override fun observeCounts(): Flow<PlaceCounts> =
        combine(dao.observeTotal(), dao.observeUnconfirmed()) { total, unconfirmed -> PlaceCounts(total, unconfirmed) }

    override fun observeLatest(): Flow<PlaceWithVisits?> = dao.observeLatest().map { it?.toModel() }

    override fun observe(id: String): Flow<PlaceWithVisits?> =
        combine(dao.observe(id), visits.observeNotesFor(id)) { row, notes -> row?.toModel(notes) }

    override suspend fun get(id: String): Place? = dao.get(id)?.toModel()

    override suspend fun getWithVisits(id: String): PlaceWithVisits? = dao.row(id)?.toModel(visits.notesFor(id))

    override suspend fun insert(place: Place) = dao.insert(place.toEntity())

    override suspend fun update(place: Place) = dao.update(place.toEntity())

    override suspend fun deleteNow(id: String) = dao.delete(id)

    override suspend fun findByProvider(provider: String, providerId: String): Place? =
        dao.findByProvider(provider, providerId)?.toModel()

    override suspend fun findSimilar(name: String, point: GeoPoint, withinM: Double): Place? {
        val r = Geo.cellRange(point, withinM)
        val key = TextNormalizer.compact(name)
        return dao.anyInCells(r.latFrom, r.latTo, r.lonFrom, r.lonTo).map { it.toModel() }.firstOrNull { p ->
            val pp = p.point ?: return@firstOrNull false
            val n = p.name ?: return@firstOrNull false
            TextNormalizer.compact(n) == key && Geo.distanceM(pp, point) <= withinM
        }
    }

    override suspend fun nearbyConfirmed(center: GeoPoint, radiusM: Double): List<PlaceWithVisits> {
        val r = Geo.cellRange(center, radiusM)
        return dao.inCells(r.latFrom, r.latTo, r.lonFrom, r.lonTo)
            .map { it.toModel() }
            .filter { pw -> pw.place.point?.let { Geo.distanceM(it, center) <= radiusM } == true }
    }

    override suspend fun allConfirmed(): List<PlaceWithVisits> = dao.allConfirmed().map { it.toModel() }

    override suspend fun mergeInto(targetId: String, from: Place) {
        db.withTransaction {
            val target = dao.get(targetId)?.toModel() ?: return@withTransaction
            val extra = target.cues.extraUtterances + listOfNotNull(from.utterance) + from.cues.extraUtterances
            val merged = target.copy(
                cues = target.cues.copy(
                    extraUtterances = extra.distinct(),
                    appearance = (target.cues.appearance + from.cues.appearance).distinct(),
                    memo = target.cues.memo ?: from.cues.memo,
                ),
            )
            dao.update(merged.toEntity())
            if (from.id != targetId) dao.delete(from.id)
        }
    }

    override suspend fun markDeleted(id: String, at: Long) = dao.markDeleted(id, at)

    override suspend fun restore(id: String) = dao.restore(id)

    override suspend fun purgeMarkedDeleted() = dao.purgeMarkedDeleted()

    override suspend fun staleDrafts(olderThan: Long): List<Place> = dao.staleDrafts(olderThan).map { it.toModel() }

    override suspend fun unconfirmedWithoutCandidates(): List<Place> = dao.unconfirmedWithoutCandidates().map { it.toModel() }

    override suspend fun insertAll(places: List<Place>) {
        db.withTransaction { dao.insertAll(places.map { it.toEntity() }) }
    }

    override suspend fun deleteEverything() {
        db.withTransaction {
            db.visitSessionDao().clear()
            visits.deleteAll()
            dao.deleteAll()
            db.shareDao().deleteAll()
            db.poiCacheDao().deleteAll()
        }
    }

    override suspend fun setLastReminded(id: String, at: Long) = dao.setLastReminded(id, at)
}
