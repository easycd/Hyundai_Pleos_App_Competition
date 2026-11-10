package com.easycd.nextplace.feature.voicesave.di

import com.easycd.nextplace.core.domain.contract.DataResetHook
import com.easycd.nextplace.core.domain.contract.SaveRunner
import com.easycd.nextplace.feature.voicesave.recovery.ResearchResetHook
import com.easycd.nextplace.feature.voicesave.pipeline.SavePipeline
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceSaveModule {
    @Binds abstract fun saveRunner(impl: SavePipeline): SaveRunner

    /** 모두 삭제: 미확인 가게 재검색 예약도 해제. */
    @Binds @IntoSet abstract fun researchReset(impl: ResearchResetHook): DataResetHook
}
