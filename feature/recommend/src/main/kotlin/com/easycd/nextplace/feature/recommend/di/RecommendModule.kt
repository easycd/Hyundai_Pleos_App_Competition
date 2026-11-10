package com.easycd.nextplace.feature.recommend.di

import com.easycd.nextplace.core.domain.contract.RecommendRunner
import com.easycd.nextplace.feature.recommend.session.RecommendSession
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RecommendModule {
    @Binds abstract fun runner(impl: RecommendSession): RecommendRunner
}
