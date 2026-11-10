package com.easycd.nextplace.feature.voicesave.pipeline.stage

import com.easycd.nextplace.core.domain.pipeline.Stage
import com.easycd.nextplace.core.domain.voice.IntentParser
import com.easycd.nextplace.core.domain.voice.ParsedUtterance
import com.easycd.nextplace.core.domain.voice.VoiceStage
import javax.inject.Inject

/** 3단계 의도·슬롯 (50ms 안). 홈 단계에서 CANCEL → RECOMMEND → NOTE → SAVE 순서. */
class ParseStage @Inject constructor(private val parser: IntentParser) : Stage<String, ParsedUtterance> {
    override val name = "3 Parse"
    override suspend fun run(input: String): ParsedUtterance = parser.parse(input, VoiceStage.HOME)
}
