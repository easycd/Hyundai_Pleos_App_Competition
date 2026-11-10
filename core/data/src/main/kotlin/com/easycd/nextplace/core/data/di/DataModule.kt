package com.easycd.nextplace.core.data.di

import android.content.Context
import androidx.room.Room
import com.easycd.nextplace.core.data.db.NextPlaceDb
import com.easycd.nextplace.core.data.prefs.DataStoreSettingsRepository
import com.easycd.nextplace.core.data.repository.RoomPlaceRepository
import com.easycd.nextplace.core.data.repository.RoomPoiCacheStore
import com.easycd.nextplace.core.data.repository.RoomShareRepository
import com.easycd.nextplace.core.data.repository.RoomVisitRepository
import com.easycd.nextplace.core.data.repository.RoomVisitSessionRepository
import com.easycd.nextplace.core.domain.poi.PoiCacheStore
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.repository.SettingsRepository
import com.easycd.nextplace.core.domain.repository.ShareRepository
import com.easycd.nextplace.core.domain.repository.VisitRepository
import com.easycd.nextplace.core.domain.repository.VisitSessionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DbModule {
    @Provides
    @Singleton
    fun db(@ApplicationContext context: Context): NextPlaceDb =
        Room.databaseBuilder(context, NextPlaceDb::class.java, NextPlaceDb.NAME).build()
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds @Singleton abstract fun places(impl: RoomPlaceRepository): PlaceRepository
    @Binds @Singleton abstract fun visits(impl: RoomVisitRepository): VisitRepository
    @Binds @Singleton abstract fun sessions(impl: RoomVisitSessionRepository): VisitSessionRepository
    @Binds @Singleton abstract fun shares(impl: RoomShareRepository): ShareRepository
    @Binds @Singleton abstract fun poiCache(impl: RoomPoiCacheStore): PoiCacheStore
    @Binds @Singleton abstract fun settings(impl: DataStoreSettingsRepository): SettingsRepository
}
