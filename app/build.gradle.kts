plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("nextplace.compose")
    id("nextplace.hilt")
}

android {
    namespace = "com.easycd.nextplace"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.easycd.nextplace"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        // x86_64 에뮬레이터에서 카카오맵(ARM 전용 엔진)을 보려면 -Pnextplace.abi=arm64-v8a 로 ARM 만 넣어 빌드한다 (ARM 변환으로 실행)
        providers.gradleProperty("nextplace.abi").orNull?.let { ndk { abiFilters += it.split(',') } }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // 공모전 제출용 APK: 키스토어가 없으면 디버그 키로 서명해 설치 가능하게 둔다.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    testOptions.unitTests.isReturnDefaultValues = true
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        // 차량·에뮬레이터에 없는 옛 CPU(mips·armeabi)용 JNA 라이브러리는 뺀다
        jniLibs.excludes += listOf("lib/mips/**", "lib/mips64/**", "lib/armeabi/**")
    }
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:pleos"))
    implementation(project(":core:poi"))
    implementation(project(":core:map"))
    implementation(project(":core:ui"))
    implementation(project(":feature:voicesave"))
    implementation(project(":feature:remind"))
    implementation(project(":feature:recommend"))
    implementation(project(":feature:visit"))
    implementation(project(":feature:myplaces"))
    implementation(project(":feature:share"))
    implementation(project(":feature:settings"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.androidx.compiler)
    implementation(libs.work.runtime)

    testImplementation(libs.junit4)
    testImplementation(libs.konsist)
}
