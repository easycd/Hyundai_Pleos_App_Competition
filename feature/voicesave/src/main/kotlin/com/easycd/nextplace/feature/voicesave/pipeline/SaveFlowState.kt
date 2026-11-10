package com.easycd.nextplace.feature.voicesave.pipeline

import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.PoiCandidate

/** 진행 중인 음성 저장의 상태 하나 (R6). 화면(S-02a~c, S-14, S-03/03b)은 이것만 그린다. */
sealed interface SaveFlowState {
    data object Idle : SaveFlowState

    /** S-02a 듣는 중. */
    data class Listening(val partial: String, val level: Float) : SaveFlowState

    /** S-02b 알아듣는 중. */
    data class Understanding(val text: String) : SaveFlowState

    /** S-02c 다시 말해 주세요. */
    data object Retry : SaveFlowState

    /** S-14 가게 찾는 중. */
    data class Searching(val text: String, val target: GeoPoint?, val current: GeoPoint?) : SaveFlowState

    /** S-03 / S-03b 확인. [waitStartedAt]은 TTS 가 끝난 시각 (카운트다운 시작). */
    data class Confirming(
        val candidate: PoiCandidate,
        val second: Boolean,
        val categoryLabel: String?,
        val distanceFromSpokenM: Int,
        val ahead: Boolean,
        val waitStartedAt: Long?,
    ) : SaveFlowState
}

/** 확인 화면 버튼 대답. */
enum class ConfirmAnswer { YES, NO }
