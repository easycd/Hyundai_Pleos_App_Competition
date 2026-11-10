package com.easycd.nextplace.core.domain.model

/** 발화 속 시점 단서 (기획서 6장). */
enum class TimeCue { JUST, EARLIER, HERE, NONE }

/** 발화 속 방향 단서. OPPOSITE(건너편)는 우측 통행 기준 왼쪽으로 본다. */
enum class DirectionCue { LEFT, RIGHT, OPPOSITE, BOTH }

/**
 * 발화에서 뽑은 단서 (기획서 6장 슬롯). 고치지 않는 기록이다 (Place.cues).
 * 외관 단서는 매칭에 쓰지 않고 정리 화면에서 메모처럼 보여 준다.
 */
data class Cues(
    val category: String? = null,
    val time: TimeCue = TimeCue.NONE,
    val direction: DirectionCue = DirectionCue.BOTH,
    val appearance: List<String> = emptyList(),
    val nameHint: String? = null,
    val memo: String? = null,
    val extraUtterances: List<String> = emptyList(),
)
