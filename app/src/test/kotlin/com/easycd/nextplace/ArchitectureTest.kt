package com.easycd.nextplace

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * 기술서 3장 규칙 (R1 기능끼리 참조 금지, 화면 5파일, domain 은 Android 없음) 을 코드로 검사한다.
 * 경로는 '/' 로 맞춰 비교한다 (Windows 에서는 '\\' 라 검사 대상이 비어 그냥 통과하던 문제). 규칙마다 대상이 비어 있지 않은지도 본다.
 */
class ArchitectureTest {
    private val main = Konsist.scopeFromProduction()
    private val features = listOf("voicesave", "remind", "recommend", "visit", "myplaces", "share", "settings")
    private val KoFileDeclaration.unixPath: String get() = path.replace('\\', '/')

    private fun <T> List<T>.nonEmpty(rule: String): List<T> = also { org.junit.Assert.assertTrue("$rule: 검사 대상이 비었음", it.isNotEmpty()) }

    @Test
    fun scopeSeesWholeProject() {
        // 검사가 빈 범위에서 그냥 통과하지 않도록: 화면 Route 14개 이상, 파일 200개 이상.
        org.junit.Assert.assertTrue(main.files.count { it.name.endsWith("Route") && it.unixPath.contains("/feature/") } >= 14)
        org.junit.Assert.assertTrue(main.files.size >= 200)
    }

    @Test
    fun featuresDoNotImportEachOther() {
        features.forEach { f ->
            main.files.filter { it.packagee?.name?.startsWith("com.easycd.nextplace.feature.$f") == true }.nonEmpty("feature $f").assertFalse { file ->
                file.imports.any { imp ->
                    imp.name.startsWith("com.easycd.nextplace.feature.") && !imp.name.startsWith("com.easycd.nextplace.feature.$f.")
                }
            }
        }
    }

    @Test
    fun domainHasNoAndroid() {
        main.files.filter { it.unixPath.contains("/core/domain/") }.nonEmpty("domain").assertFalse { file ->
            file.imports.any { it.name.startsWith("android.") || it.name.startsWith("androidx.") }
        }
    }

    @Test
    fun coreDoesNotImportFeatures() {
        main.files.filter { it.unixPath.contains("/core/") }.nonEmpty("core").assertFalse { file ->
            file.imports.any { it.name.startsWith("com.easycd.nextplace.feature.") }
        }
    }

    @Test
    fun screensOnlyDrawState() {
        // Screen 은 UiState·onAction 만 받는 순수 함수: ViewModel·Hilt·Repository 를 모른다.
        main.files.filter { it.unixPath.contains("/feature/") && it.name.endsWith("Screen") }.nonEmpty("screens").assertFalse { file ->
            file.imports.any { imp ->
                imp.name.contains("hiltViewModel") || imp.name.endsWith("ViewModel") || imp.name.contains(".repository.")
            }
        }
    }

    @Test
    fun viewModelsAreHilt() {
        main.classes().withNameEndingWith("ViewModel").nonEmpty("view models").assertTrue { it.hasAnnotationWithName("HiltViewModel") }
    }

    @Test
    fun everyRouteHasItsFiveFiles() {
        val routes = main.files.filter { it.unixPath.contains("/feature/") && it.name.endsWith("Route") }.nonEmpty("routes")
        routes.assertTrue { route ->
            val dir = route.unixPath.substringBeforeLast('/')
            val base = route.name.removeSuffix("Route")
            val names = main.files.filter { it.unixPath.substringBeforeLast('/') == dir }.map { it.name }.toSet()
            listOf("ViewModel", "UiState", "Action").all { "$base$it" in names }
        }
    }
}
