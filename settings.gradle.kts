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
