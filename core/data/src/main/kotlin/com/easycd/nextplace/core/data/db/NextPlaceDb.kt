package com.easycd.nextplace.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.easycd.nextplace.core.data.entity.PlaceEntity
import com.easycd.nextplace.core.data.entity.PoiCacheEntity
import com.easycd.nextplace.core.data.entity.ShareListEntity
import com.easycd.nextplace.core.data.entity.VisitEntity
import com.easycd.nextplace.core.data.entity.VisitSessionEntity

/**
 * 앱 DB (기술서 8장). 1차 접수 빌드부터는 버전마다 Migration 클래스를 db/migration/ 에 둔다.
 * v1 은 첫 버전이라 마이그레이션이 없다.
 */
@Database(
    entities = [PlaceEntity::class, VisitEntity::class, VisitSessionEntity::class, ShareListEntity::class, PoiCacheEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class NextPlaceDb : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun visitDao(): VisitDao
    abstract fun visitSessionDao(): VisitSessionDao
    abstract fun shareDao(): ShareDao
    abstract fun poiCacheDao(): PoiCacheDao

    companion object {
        const val NAME = "nextplace.db"
        const val POI_CACHE_MAX = 2_000
    }
}
