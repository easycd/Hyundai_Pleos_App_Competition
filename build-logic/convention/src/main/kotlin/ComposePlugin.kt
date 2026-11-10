import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class ComposePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        val android = extensions.getByName("android") as CommonExtension<*, *, *, *, *, *>
        android.buildFeatures.compose = true
        dependencies {
            val bom = platform(libs.findLibrary("compose-bom").get())
            add("implementation", bom)
            listOf(
                "compose-ui", "compose-ui-graphics", "compose-foundation", "compose-material3",
                "compose-animation", "compose-ui-tooling-preview",
                "androidx-lifecycle-runtime-compose", "androidx-lifecycle-viewmodel-compose",
            ).forEach { add("implementation", libs.findLibrary(it).get()) }
            add("debugImplementation", libs.findLibrary("compose-ui-tooling").get())
        }
    }
}
