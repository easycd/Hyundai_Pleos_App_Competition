package com.easycd.nextplace.core.data.repository

import com.easycd.nextplace.core.data.db.NextPlaceDb
import com.easycd.nextplace.core.data.entity.PoiCacheEntity
import com.easycd.nextplace.core.data.entity.ShareListEntity
import com.easycd.nextplace.core.data.mapper.DbJson
import com.easycd.nextplace.core.data.mapper.toEntity
import com.easycd.nextplace.core.data.mapper.toModel
import com.easycd.nextplace.core.domain.model.ShareList
import com.easycd.nextplace.core.domain.model.Visit
import com.easycd.nextplace.core.domain.model.VisitSession
import com.easycd.nextplace.core.domain.poi.CachedPoi
import com.easycd.nextplace.core.domain.poi.PoiCacheStore
import com.easycd.nextplace.core.domain.repository.ShareRepository
import com.easycd.nextplace.core.domain.repository.VisitRepository
import com.easycd.nextplace.core.domain.repository.VisitSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class RoomVisitRepository @Inject constructor(db: NextPlaceDb) : VisitRepository {
    private val dao = db.visitDao()
    override suspend fun insert(visit: Visit) = dao.insert(visit.toEntity())
    override suspend fun get(id: String): Visit? = dao.get(id)?.toModel()
    override suspend fun setLeftAt(id: String, at: Long) = dao.setLeftAt(id, at)
    override suspend fun setNote(id: String, note: String) = dao.setNote(id, note)
    override suspend fun latestSince(since: Long): Visit? = dao.latestSince(since)?.toModel()
    override suspend fun lastFor(placeId: String): Visit? = dao.lastFor(placeId)?.toModel()
    override suspend fun countFor(placeId: String): Int = dao.countFor(placeId)
}

internal class RoomVisitSessionRepository @Inject constructor(db: NextPlaceDb) : VisitSessionRepository {
    private val dao = db.visitSessionDao()
    override fun observe(): Flow<VisitSession?> = dao.observe().map { it?.toModel() }
    override suspend fun get(): VisitSession? = dao.get()?.toModel()
    override suspend fun save(session: VisitSession) = dao.replace(session.toEntity())
    override suspend fun clear() = dao.clear()
}

internal class RoomShareRepository @Inject constructor(db: NextPlaceDb) : ShareRepository {
    private val dao = db.shareDao()
    override suspend fun save(list: ShareList) =
        dao.insert(ShareListEntity(list.id, list.title, DbJson.shared(list.places), list.createdAt))
}

internal class RoomPoiCacheStore @Inject constructor(db: NextPlaceDb) : PoiCacheStore {
    private val dao = db.poiCacheDao()
    override suspend fun get(key: String): CachedPoi? = dao.get(key)?.let { CachedPoi(it.json, it.savedAt) }
    override suspend fun put(key: String, json: String, savedAt: Long) {
        dao.put(PoiCacheEntity(key, json, savedAt))
        dao.trim(NextPlaceDb.POI_CACHE_MAX)
    }
    override suspend fun clear() = dao.deleteAll()
}
