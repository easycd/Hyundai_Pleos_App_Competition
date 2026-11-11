import java.util.Properties

plugins {
    id("nextplace.android.library")
    id("nextplace.compose")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    buildFeatures { buildConfig = true }
    defaultConfig {
        // 카카오맵 네이티브 앱 키도 저장소에 올리지 않는다. local.properties 의 KAKAO_NATIVE_KEY 에 넣는다.
        // 비어 있으면 지도 SDK 를 띄우지 않고 배경 없는 핀 지도로 그린다.
        val key = (localProps.getProperty("KAKAO_NATIVE_KEY") ?: "").trim()
        buildConfigField("String", "KAKAO_NATIVE_KEY", "\"$key\"")
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(libs.kakao.maps)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
