package com.easycd.nextplace.core.domain

import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.voice.IntentParser
import com.easycd.nextplace.core.domain.voice.SlotExtractor
import com.easycd.nextplace.core.domain.voice.VoiceStage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** 전수점검 보고서(docs/05) 재현 테스트: B-02 · B-22. */
class AuditDomainTest {
    @Test fun coldStartImportSurvivesUntilNavigationCollectorStarts() = runBlocking {
        val navigator = Navigator()
        val wanted = Destination.Import("https://easycd.github.io/nextplace/s#test")
        navigator.go(wanted)
        assertEquals(wanted, withTimeoutOrNull(150) { navigator.commands.first() })
    }

    @Test fun elevenIsNotSilentlySelectedAsOne() {
        val parser = IntentParser(SlotExtractor())
        val parsed = parser.parse("11번", VoiceStage.RECOMMEND, listOf("서울갈비", "커피", "국밥"))
        assertEquals(10, parsed.selectedIndex)
    }
}
