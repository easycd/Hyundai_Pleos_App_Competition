package com.easycd.nextplace.feature.voicesave.di

import com.easycd.nextplace.core.domain.contract.SaveRunner
import com.easycd.nextplace.feature.voicesave.pipeline.SavePipeline
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceSaveModule {
    @Binds abstract fun saveRunner(impl: SavePipeline): SaveRunner
}
