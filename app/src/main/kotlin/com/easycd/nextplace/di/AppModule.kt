package com.easycd.nextplace.di

import com.easycd.nextplace.core.domain.pipeline.TraceLog
import com.easycd.nextplace.log.FileTraceLog
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds @Singleton abstract fun trace(impl: FileTraceLog): TraceLog
}
