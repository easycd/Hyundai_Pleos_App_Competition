pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://nexus-playground.pleos.ai/repository/maven-releases/")
    }
}

rootProject.name = "NextPlace"

include(":app")
include(":core:domain")
include(":core:data")
include(":core:pleos")
include(":core:poi")
include(":core:ui")
include(":feature:voicesave")
include(":feature:remind")
include(":feature:recommend")
include(":feature:visit")
include(":feature:myplaces")
include(":feature:share")
include(":feature:settings")

// 화면 렌더링 검사 (에뮬레이터 없이 Paparazzi 로 S-xx 화면을 PNG 로 그림). 필요할 때만: ./gradlew -Pnextplace.screens=true :tools:screenshots:recordPaparazziDebug
if (providers.gradleProperty("nextplace.screens").orNull == "true") include(":tools:screenshots")
