plugins {
    id("nextplace.android.library")
    id("nextplace.compose")
    id("app.cash.paparazzi") version "1.3.5"
}


dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":feature:voicesave"))
    implementation(project(":feature:remind"))
    implementation(project(":feature:recommend"))
    implementation(project(":feature:visit"))
    implementation(project(":feature:myplaces"))
    implementation(project(":feature:share"))
    implementation(project(":feature:settings"))
}
