package com.easycd.nextplace

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * 모듈마다 같은 이름의 문자열이 다른 값을 가지면 합칠 때 하나가 다른 하나를 덮어써 엉뚱한 문구가 나온다.
 * 모든 모듈의 values/ 문자열을 모아 이름이 겹치는데 값이 다른 것을 찾는다.
 */
class ResourceNamesTest {
    @Test
    fun noConflictingStringNames() {
        val root = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }
        val seen = HashMap<String, Pair<String, String>>()
        val conflicts = mutableListOf<String>()
        root.walkTopDown()
            .onEnter { it.name != "build" && it.name != ".gradle" && it.name != "docs" }
            .filter { it.isFile && it.extension == "xml" && it.parentFile.name == "values" && it.path.contains("src${File.separator}main") }
            .forEach { f ->
                val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f)
                val nodes = doc.getElementsByTagName("string")
                for (i in 0 until nodes.length) {
                    val n = nodes.item(i)
                    val name = n.attributes.getNamedItem("name").nodeValue
                    val value = n.textContent
                    val prev = seen.putIfAbsent(name, value to f.relativeTo(root).path)
                    if (prev != null && prev.first != value) conflicts += "$name: ${prev.second}=\"${prev.first}\" vs ${f.relativeTo(root).path}=\"$value\""
                }
            }
        assertTrue("겹치는 문자열 이름:\n" + conflicts.joinToString("\n"), conflicts.isEmpty())
        assertTrue("문자열을 하나도 못 찾음", seen.size > 50)
    }
}
