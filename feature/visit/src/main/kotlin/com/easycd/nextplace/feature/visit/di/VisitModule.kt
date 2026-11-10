package com.easycd.nextplace.feature.visit.di

import com.easycd.nextplace.core.domain.contract.VisitNoteRunner
import com.easycd.nextplace.feature.visit.note.AttachNoteToRecentVisit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class VisitModule {
    @Binds abstract fun noteRunner(impl: AttachNoteToRecentVisit): VisitNoteRunner
}
