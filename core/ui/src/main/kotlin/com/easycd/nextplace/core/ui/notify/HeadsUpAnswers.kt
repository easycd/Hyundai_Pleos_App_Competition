package com.easycd.nextplace.core.ui.notify

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** 차량 알림 버튼 대답 하나: [positive] = 오른쪽(실행) 버튼. */
data class HeadsUpAnswer(val id: Int, val positive: Boolean)

/** 알림 버튼 → 기능(리마인드 · 방문 메모) 통로. 받는 쪽이 카드를 띄운 동안만 듣는다. */
@Singleton
class HeadsUpAnswers @Inject constructor() {
    private val _answers = MutableSharedFlow<HeadsUpAnswer>(extraBufferCapacity = 4, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun emit(answer: HeadsUpAnswer) {
        _answers.tryEmit(answer)
    }

    /** [id] 알림의 버튼만: true = 실행 버튼. */
    fun of(id: Int): Flow<Boolean> = _answers.asSharedFlow().filter { it.id == id }.map { it.positive }
}
