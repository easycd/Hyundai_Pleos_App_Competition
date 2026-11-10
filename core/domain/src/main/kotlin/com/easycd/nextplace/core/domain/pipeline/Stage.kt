package com.easycd.nextplace.core.domain.pipeline

import com.easycd.nextplace.core.domain.sdk.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * 파이프라인 단계 (기술서 7장). 단계마다 이름·제한 시간이 있고, 실행기는 걸린 시간을 기록한다.
 * 실패 시 다음 행동은 파이프라인이 [StageOutcome]을 보고 정한다 (대부분 미확인 저장).
 */
interface Stage<I, O> {
    val name: String
    val timeoutMs: Long? get() = null
    suspend fun run(input: I): O
}

sealed interface StageOutcome<out O> {
    data class Ok<O>(val value: O) : StageOutcome<O>
    data object TimedOut : StageOutcome<Nothing>
    data class Failed(val error: Throwable) : StageOutcome<Nothing>
}

/** 단계별 시간 기록과 로그 (기술서 9장 측정, 10장 로그 파일). 구현은 app. */
interface TraceLog {
    fun stage(pipeline: String, stage: String, elapsedMs: Long, outcome: String)
    fun event(tag: String, message: String)
    fun error(tag: String, error: Throwable)
}

/** 단계 실행기: 제한 시간, 예외 → 결과 값 (R9), 시간 기록. */
class StageRunner @Inject constructor(private val clock: Clock, private val trace: TraceLog) {
    suspend fun <I, O> run(pipeline: String, stage: Stage<I, O>, input: I): StageOutcome<O> {
        val start = clock.now()
        val outcome: StageOutcome<O> = try {
            val timeout = stage.timeoutMs
            if (timeout == null) {
                StageOutcome.Ok(stage.run(input))
            } else {
                withTimeoutOrNull(timeout) { StageOutcome.Ok(stage.run(input)) } ?: StageOutcome.TimedOut
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            trace.error("$pipeline/${stage.name}", e)
            StageOutcome.Failed(e)
        }
        trace.stage(pipeline, stage.name, clock.now() - start, outcome::class.simpleName ?: "")
        return outcome
    }
}
