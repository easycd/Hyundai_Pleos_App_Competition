import java.util.Properties

plugins {
    id("nextplace.android.library")
    id("nextplace.hilt")
    alias(libs.plugins.kotlin.serialization)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    useLibrary("android.car")
    buildFeatures { buildConfig = true }
    defaultConfig {
        val fakeSdk = providers.gradleProperty("nextplace.fakeSdk").getOrElse("false").toBoolean()
        val fakeLocation = providers.gradleProperty("nextplace.fakeLocation").getOrElse("false").toBoolean()
        buildConfigField("boolean", "FAKE_SDK", fakeSdk.toString())
        buildConfigField("boolean", "FAKE_LOCATION", fakeLocation.toString())
        // 시연 녹화용: 말소리 대신 adb 방송으로 문장을 넣는다 (STT 만 가짜, TTS·위치·내비는 실제). 기본 빌드에서는 꺼져 있음
        val demoVoice = providers.gradleProperty("nextplace.demoVoice").getOrElse("false").toBoolean()
        buildConfigField("boolean", "DEMO_VOICE", demoVoice.toString())
        // Pleos Playground 자격 증명 (STT·TTS 서버 모드). 저장소에 올리지 않는다.
        buildConfigField("String", "PLEOS_CLIENT_ID", "\"${localProps.getProperty("PLEOS_CLIENT_ID") ?: ""}\"")
        buildConfigField("String", "PLEOS_CLIENT_SECRET", "\"${localProps.getProperty("PLEOS_CLIENT_SECRET") ?: ""}\"")
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.pleos.stt)
    implementation(libs.pleos.tts)
    implementation(libs.pleos.location)
    implementation(libs.pleos.navi)
    implementation(libs.pleos.vehicle)
    implementation(libs.kotlinx.serialization.json)
    constraints {
        implementation(libs.jna) { because("16KB 페이지 정렬된 libjnidispatch (5.17.0부터)") }
    }
}
