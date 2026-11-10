plugins {
    id("nextplace.android.feature")
}

dependencies {
    // 미확인 가게 후보 재검색 (기술서 10장: 네트워크 복구 후 1회)
    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.hilt.androidx.compiler)
}
