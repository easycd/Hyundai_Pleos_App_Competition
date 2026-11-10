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
    buildFeatures { buildConfig = true }
    defaultConfig {
        // 카카오 REST 키는 저장소에 올리지 않는다. local.properties 의 KAKAO_REST_KEY 에 넣는다.
        val key = (localProps.getProperty("KAKAO_REST_KEY") ?: "").trim()
        buildConfigField("String", "KAKAO_REST_KEY", "\"$key\"")
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
}
