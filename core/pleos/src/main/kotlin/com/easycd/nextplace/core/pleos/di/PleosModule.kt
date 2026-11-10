package com.easycd.nextplace.core.pleos.di

import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.DevVoiceInput
import com.easycd.nextplace.core.domain.sdk.LocationSource
import com.easycd.nextplace.core.domain.sdk.NaviController
import com.easycd.nextplace.core.domain.sdk.PermissionChecker
import com.easycd.nextplace.core.domain.sdk.SpeechRecognizer
import com.easycd.nextplace.core.domain.sdk.SpeechSynthesizer
import com.easycd.nextplace.core.domain.sdk.UxRestrictionSource
import com.easycd.nextplace.core.pleos.BuildConfig
import com.easycd.nextplace.core.pleos.location.FakeLocationFeed
import com.easycd.nextplace.core.pleos.location.LocationFeed
import com.easycd.nextplace.core.pleos.location.LocationTracker
import com.easycd.nextplace.core.pleos.location.RealLocationFeed
import com.easycd.nextplace.core.pleos.navi.FakeNaviController
import com.easycd.nextplace.core.pleos.navi.RealNaviController
import com.easycd.nextplace.core.pleos.stt.FakeSpeechRecognizer
import com.easycd.nextplace.core.pleos.stt.RealSpeechRecognizer
import com.easycd.nextplace.core.pleos.system.AndroidPermissionChecker
import com.easycd.nextplace.core.pleos.system.SystemClock
import com.easycd.nextplace.core.pleos.tts.FakeSpeechSynthesizer
import com.easycd.nextplace.core.pleos.tts.RealSpeechSynthesizer
import com.easycd.nextplace.core.pleos.vehicle.FakeUxRestrictionSource
import com.easycd.nextplace.core.pleos.vehicle.RealUxRestrictionSource
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Real/Fake 선택 (기술서 2장·10장). gradle.properties 의 nextplace.fakeSdk=true 면 가짜 SDK,
 * nextplace.fakeLocation=true 면 실제 SDK 를 쓰면서 위치만 경로 재생.
 * Lazy 로 받아 쓰지 않는 쪽은 만들지 않는다.
 */
@Module
@InstallIn(SingletonComponent::class)
object PleosModule {
    private val fake = BuildConfig.FAKE_SDK

    @Provides @Singleton
    fun recognizer(real: Lazy<RealSpeechRecognizer>, fakeImpl: Lazy<FakeSpeechRecognizer>): SpeechRecognizer =
        if (fake) fakeImpl.get() else real.get()

    @Provides @Singleton
    fun devVoice(fakeImpl: Lazy<FakeSpeechRecognizer>): DevVoiceInput =
        if (fake) fakeImpl.get() else DisabledDevVoice

    @Provides @Singleton
    fun synthesizer(real: Lazy<RealSpeechSynthesizer>, fakeImpl: Lazy<FakeSpeechSynthesizer>): SpeechSynthesizer =
        if (fake) fakeImpl.get() else real.get()

    @Provides @Singleton
    fun feed(real: Lazy<RealLocationFeed>, fakeImpl: Lazy<FakeLocationFeed>): LocationFeed =
        if (fake || BuildConfig.FAKE_LOCATION) fakeImpl.get() else real.get()

    @Provides @Singleton
    fun navi(real: Lazy<RealNaviController>, fakeImpl: Lazy<FakeNaviController>): NaviController =
        if (fake) fakeImpl.get() else real.get()

    @Provides @Singleton
    fun ux(real: Lazy<RealUxRestrictionSource>, fakeImpl: Lazy<FakeUxRestrictionSource>): UxRestrictionSource =
        if (fake) fakeImpl.get() else real.get()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PleosBindings {
    @Binds @Singleton abstract fun location(impl: LocationTracker): LocationSource
    @Binds @Singleton abstract fun clock(impl: SystemClock): Clock
    @Binds @Singleton abstract fun permissions(impl: AndroidPermissionChecker): PermissionChecker
}

private object DisabledDevVoice : DevVoiceInput {
    override val enabled = false
    override val listening = kotlinx.coroutines.flow.MutableStateFlow(false)
    override fun submit(text: String) = Unit
}
