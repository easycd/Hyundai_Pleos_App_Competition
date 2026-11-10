package com.easycd.nextplace.core.poi.di

import com.easycd.nextplace.core.domain.poi.PoiSource
import com.easycd.nextplace.core.domain.poi.ReverseGeocoder
import com.easycd.nextplace.core.poi.BuildConfig
import com.easycd.nextplace.core.poi.KakaoLocalApi
import com.easycd.nextplace.core.poi.KakaoPoiSource
import com.easycd.nextplace.core.poi.KakaoReverseGeocoder
import com.easycd.nextplace.core.poi.NoReverseGeocoder
import com.easycd.nextplace.core.poi.SamplePoiSource
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/** 카카오 키(local.properties KAKAO_REST_KEY)가 있으면 카카오, 없으면 내장 샘플 (기술서 10장 표). */
@Module
@InstallIn(SingletonComponent::class)
internal object PoiModule {
    private val hasKey get() = BuildConfig.KAKAO_REST_KEY.isNotBlank()

    @Provides
    @Singleton
    fun api(): KakaoLocalApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("Authorization", "KakaoAK ${BuildConfig.KAKAO_REST_KEY}").build())
            }
            .build()
        return Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com/")
            .client(client)
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(KakaoLocalApi::class.java)
    }

    @Provides
    @Singleton
    fun poi(kakao: Lazy<KakaoPoiSource>, sample: Lazy<SamplePoiSource>): PoiSource = if (hasKey) kakao.get() else sample.get()

    @Provides
    @Singleton
    fun geocoder(kakao: Lazy<KakaoReverseGeocoder>, none: Lazy<NoReverseGeocoder>): ReverseGeocoder =
        if (hasKey) kakao.get() else none.get()

    private const val TIMEOUT_MS = 1_200L
}
