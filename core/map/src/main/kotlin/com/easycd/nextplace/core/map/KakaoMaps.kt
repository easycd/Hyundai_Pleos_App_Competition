package com.easycd.nextplace.core.map

import android.content.Context
import android.os.Build
import android.util.Log
import com.easycd.nextplace.core.ui.component.MapRenderer
import com.kakao.vectormap.KakaoMapSdk

/**
 * 카카오맵 SDK 준비 (기획서 F-12, 기술서 2장 외부 API). 앱 시작 때 한 번 [init]하고, 쓸 수 있으면 [renderer]를 돌려준다.
 * 지도를 띄우지 않는 경우: 키가 없음 · 이 앱이 설치된 CPU 용 지도 엔진이 없음(SDK 는 ARM 만 제공) · 초기화 실패.
 * 그때는 null 이라 화면은 배경 없는 핀 지도로 그린다.
 */
object KakaoMaps {
    private var ready = false

    fun init(context: Context) {
        val key = BuildConfig.KAKAO_NATIVE_KEY
        if (key.isBlank()) {
            Log.i(TAG, "kakao map off: KAKAO_NATIVE_KEY is empty, using the pin map")
            return
        }
        // 엔진은 ARM 용만 있다. x86_64 로 설치된 앱(에뮬레이터)에는 없으므로 먼저 불러 보고 없으면 핀 지도로 간다.
        val engine = runCatching { System.loadLibrary(ENGINE_LIB) }
        if (engine.isFailure) {
            Log.w(TAG, "kakao map skipped: engine not available for ${Build.SUPPORTED_ABIS.firstOrNull()} (build with -Pnextplace.abi=arm64-v8a)")
            return
        }
        ready = runCatching {
            KakaoMapSdk.init(context.applicationContext, key)
            // 카카오 개발자 콘솔의 Android 플랫폼에 등록할 키 해시 (등록 안 하면 인증 실패로 핀 지도로 돌아감)
            Log.i(TAG, "kakao map key hash: ${KakaoMapSdk.INSTANCE?.hashKey}")
            true
        }.getOrElse {
            Log.w(TAG, "kakao map init failed", it)
            false
        }
    }

    /** 지도 SDK 를 쓸 수 있으면 지도 그리개, 아니면 null. */
    fun renderer(): MapRenderer? = if (ready) KakaoMapRenderer else null

    internal const val TAG = "KakaoMaps"
    private const val ENGINE_LIB = "K3fAndroid"
}
