plugins {
    id("nextplace.android.library")
    id("nextplace.compose")
    id("nextplace.hilt")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.navigation.compose)
}
