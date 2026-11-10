plugins {
    `kotlin-dsl`
}


dependencies {
    compileOnly(libs.android.gradle)
    compileOnly(libs.kotlin.gradle)
    compileOnly(libs.compose.gradle)
    compileOnly(libs.ksp.gradle)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "nextplace.android.library"
            implementationClass = "AndroidLibraryPlugin"
        }
        register("androidFeature") {
            id = "nextplace.android.feature"
            implementationClass = "AndroidFeaturePlugin"
        }
        register("jvmLibrary") {
            id = "nextplace.jvm.library"
            implementationClass = "JvmLibraryPlugin"
        }
        register("hilt") {
            id = "nextplace.hilt"
            implementationClass = "HiltPlugin"
        }
        register("compose") {
            id = "nextplace.compose"
            implementationClass = "ComposePlugin"
        }
    }
}
