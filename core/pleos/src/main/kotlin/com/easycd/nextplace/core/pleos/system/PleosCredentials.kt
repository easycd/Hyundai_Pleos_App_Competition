package com.easycd.nextplace.core.pleos.system

import com.easycd.nextplace.core.pleos.BuildConfig

/**
 * Pleos Playground 자격 증명 (STT·TTS 서버 모드용). local.properties 의 PLEOS_CLIENT_ID / PLEOS_CLIENT_SECRET.
 * 없으면 등록이 필요 없는 ON_DEVICE 모드로 돈다 (기획서 10장 2번).
 */
internal object PleosCredentials {
    val clientId: String get() = BuildConfig.PLEOS_CLIENT_ID
    val clientSecret: String get() = BuildConfig.PLEOS_CLIENT_SECRET
    val present: Boolean get() = clientId.isNotBlank() && clientSecret.isNotBlank()
}
