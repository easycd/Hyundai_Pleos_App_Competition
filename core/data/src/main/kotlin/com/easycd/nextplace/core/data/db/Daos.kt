package com.easycd.nextplace.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.easycd.nextplace.core.data.entity.PlaceEntity
import com.easycd.nextplace.core.data.entity.PlaceRow
import com.easycd.nextplace.core.data.entity.PoiCacheEntity
import com.easycd.nextplace.core.data.entity.ShareListEntity
import com.easycd.nextplace.core.data.entity.VisitEntity
import com.easycd.nextplace.core.data.entity.VisitSessionEntity
import kotlinx.coroutines.flow.Flow

private const val ROW = """
    SELECT p.*,
      (SELECT COUNT(*) FROM visit v WHERE v.placeId = p.id) AS visitCount,
      (SELECT MAX(v.arrivedAt) FROM visit v WHERE v.placeId = p.id) AS lastVisitAt,
      (SELECT GROUP_CONCAT(v.note, char(10)) FROM visit v WHERE v.placeId = p.id AND v.note IS NOT NULL) AS notes
    FROM place p
"""
private const val VISIBLE = "p.deletedAt IS NULL AND p.draft = 0"

@Dao
interface PlaceDao {
    @Query("$ROW WHERE $VISIBLE ORDER BY p.savedAt DESC")
    fun observeAll(): Flow<List<PlaceRow>>

    @Query("$ROW WHERE $VISIBLE ORDER BY p.savedAt DESC LIMIT 1")
    fun observeLatest(): Flow<PlaceRow?>

    @Query("$ROW WHERE p.id = :id AND $VISIBLE")
    fun observe(id: String): Flow<PlaceRow?>

    @Query("$ROW WHERE p.id = :id")
    suspend fun row(id: String): PlaceRow?

    @Query("SELECT COUNT(*) FROM place p WHERE $VISIBLE")
    fun observeTotal(): Flow<Int>

    @Query("SELECT COUNT(*) FROM place p WHERE $VISIBLE AND p.status = 'UNCONFIRMED'")
    fun observeUnconfirmed(): Flow<Int>

    @Query("SELECT * FROM place WHERE id = :id")
    suspend fun get(id: String): PlaceEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(place: PlaceEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(places: List<PlaceEntity>)

    @Update
    suspend fun update(place: PlaceEntity)

    @Query("DELETE FROM place WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM place WHERE provider = :provider AND providerId = :providerId AND draft = 0 LIMIT 1")
    suspend fun findByProvider(provider: String, providerId: String): PlaceEntity?

    @Query(
        "$ROW WHERE $VISIBLE AND p.status = 'CONFIRMED' AND p.gridLat BETWEEN :latFrom AND :latTo " +
            "AND p.gridLon BETWEEN :lonFrom AND :lonTo",
    )
    suspend fun inCells(latFrom: Int, latTo: Int, lonFrom: Int, lonTo: Int): List<PlaceRow>

    @Query(
        "SELECT * FROM place p WHERE $VISIBLE AND p.gridLat BETWEEN :latFrom AND :latTo " +
            "AND p.gridLon BETWEEN :lonFrom AND :lonTo",
    )
    suspend fun anyInCells(latFrom: Int, latTo: Int, lonFrom: Int, lonTo: Int): List<PlaceEntity>

    @Query("$ROW WHERE $VISIBLE AND p.status = 'CONFIRMED'")
    suspend fun allConfirmed(): List<PlaceRow>

    @Query("UPDATE place SET deletedAt = :at WHERE id = :id")
    suspend fun markDeleted(id: String, at: Long)

    @Query("UPDATE place SET deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: String)

    @Query("DELETE FROM place WHERE deletedAt IS NOT NULL")
    suspend fun purgeMarkedDeleted()

    @Query("SELECT * FROM place WHERE draft = 1 AND savedAt < :olderThan")
    suspend fun staleDrafts(olderThan: Long): List<PlaceEntity>

    @Query(
        "SELECT * FROM place WHERE draft = 0 AND deletedAt IS NULL AND status = 'UNCONFIRMED' " +
            "AND candidatesJson = '[]' AND lat IS NOT NULL",
    )
    suspend fun unconfirmedWithoutCandidates(): List<PlaceEntity>

    @Query("UPDATE place SET lastRemindedAt = :at WHERE id = :id")
    suspend fun setLastReminded(id: String, at: Long)

    @Query("DELETE FROM place")
    suspend fun deleteAll()
}

@Dao
interface VisitDao {
    @Insert
    suspend fun insert(visit: VisitEntity)

    @Query("SELECT * FROM visit WHERE id = :id")
    suspend fun get(id: String): VisitEntity?

    @Query("UPDATE visit SET leftAt = :at WHERE id = :id")
    suspend fun setLeftAt(id: String, at: Long)

    @Query("UPDATE visit SET note = :note WHERE id = :id")
    suspend fun setNote(id: String, note: String)

    @Query("SELECT * FROM visit WHERE arrivedAt >= :since OR leftAt IS NULL OR leftAt >= :since ORDER BY arrivedAt DESC LIMIT 1")
    suspend fun latestSince(since: Long): VisitEntity?

    @Query("SELECT * FROM visit WHERE placeId = :placeId ORDER BY arrivedAt DESC LIMIT 1")
    suspend fun lastFor(placeId: String): VisitEntity?

    @Query("SELECT COUNT(*) FROM visit WHERE placeId = :placeId")
    suspend fun countFor(placeId: String): Int

    @Query("SELECT note FROM visit WHERE placeId = :placeId AND note IS NOT NULL ORDER BY arrivedAt DESC")
    suspend fun notesFor(placeId: String): List<String>

    @Query("SELECT COUNT(*) FROM visit WHERE placeId = :placeId")
    suspend fun countFor(placeId: String): Int

    @Query("SELECT note FROM visit WHERE placeId = :placeId AND note IS NOT NULL ORDER BY arrivedAt DESC")
    fun observeNotesFor(placeId: String): Flow<List<String>>

    @Query("DELETE FROM visit")
    suspend fun deleteAll()
}

@Dao
interface VisitSessionDao {
    @Query("SELECT * FROM visit_session LIMIT 1")
    fun observe(): Flow<VisitSessionEntity?>

    @Query("SELECT * FROM visit_session LIMIT 1")
    suspend fun get(): VisitSessionEntity?

    @Query("DELETE FROM visit_session")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: VisitSessionEntity)

    /** 한 번에 1개만 (다른 가게로 안내를 맡기면 새 행으로 바뀜). */
    @Transaction
    suspend fun replace(session: VisitSessionEntity) {
        clear()
        insert(session)
    }
}

@Dao
interface ShareDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ShareListEntity)

    @Query("DELETE FROM share_list")
    suspend fun deleteAll()
}

@Dao
interface PoiCacheDao {
    @Query("SELECT * FROM poi_cache WHERE `key` = :key")
    suspend fun get(key: String): PoiCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: PoiCacheEntity)

    /** 최대 [keep]건만 남기고 오래된 것부터 지운다. */
    @Query("DELETE FROM poi_cache WHERE `key` NOT IN (SELECT `key` FROM poi_cache ORDER BY savedAt DESC LIMIT :keep)")
    suspend fun trim(keep: Int)

    @Query("DELETE FROM poi_cache")
    suspend fun deleteAll()
}
