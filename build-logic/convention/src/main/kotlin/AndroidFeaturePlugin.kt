import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** feature 모듈 = Android 라이브러리 + Compose + Hilt + core(domain·ui). feature끼리는 참조하지 않는다 (R1). */
class AndroidFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("nextplace.android.library")
        pluginManager.apply("nextplace.compose")
        pluginManager.apply("nextplace.hilt")
        dependencies {
            add("implementation", project(":core:domain"))
            add("implementation", project(":core:ui"))
            add("implementation", libs.findLibrary("hilt-navigation-compose").get())
            add("implementation", libs.findLibrary("androidx-navigation-compose").get())
        }
    }
}
