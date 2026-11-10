plugins {
    id("nextplace.android.library")
    id("nextplace.compose")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.androidx.core.ktx)
}
