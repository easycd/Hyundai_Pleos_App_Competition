package com.easycd.nextplace.core.poi.di

import com.easycd.nextplace.core.domain.contract.DataResetHook
import com.easycd.nextplace.core.poi.PoiCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/** 모두 삭제: POI 메모리 캐시도 비운다 (DB 캐시 표는 PlaceRepository.deleteEverything 이 지움). */
@Module
@InstallIn(SingletonComponent::class)
internal object PoiResetModule {
    @Provides
    @IntoSet
    fun poiReset(cache: PoiCache): DataResetHook = object : DataResetHook {
        override suspend fun onAllDataDeleted() = cache.clearMemory()
    }
}
