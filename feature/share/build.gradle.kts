plugins {
    id("nextplace.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.zxing.core)
}
