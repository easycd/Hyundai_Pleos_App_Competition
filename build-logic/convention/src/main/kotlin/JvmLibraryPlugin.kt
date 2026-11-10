import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/** 순수 Kotlin 모듈 (core/domain). Android 의존 없음, JUnit5. */
class JvmLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        configureKotlin()
        tasks.withType<Test>().configureEach { useJUnitPlatform() }
        dependencies {
            add("testImplementation", libs.findLibrary("junit5-api").get())
            add("testImplementation", libs.findLibrary("junit5-params").get())
            add("testRuntimeOnly", libs.findLibrary("junit5-engine").get())
            add("testRuntimeOnly", libs.findLibrary("junit5-launcher").get())
            add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
