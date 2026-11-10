package com.easycd.nextplace.core.domain.pipeline

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.sdk.Clock
import com.easycd.nextplace.core.domain.sdk.SpeechRecognizer
import com.easycd.nextplace.core.domain.sdk.SttEvent
import com.easycd.nextplace.core.domain.sdk.SttFailure
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 듣기 결과. */
sealed interface ListenOutcome {
    data class Heard(val text: String) : ListenOutcome
    data object Empty : ListenOutcome
    data class Failed(val reason: SttFailure) : ListenOutcome
}

/** 듣는 동안의 진행 (화면 S-02a 의 인식 중 문장·소리 막대). */
fun interface ListenProgress {
    fun onEvent(event: SttEvent)
}

data class ListenRequest(
    val maxMs: Long = Tunables.LISTEN_MAX_MS,
    /** 말이 하나도 없을 때 이 시간이 지나면 끝 (확인 대답 5초 등). null 이면 [maxMs]까지. */
    val noSpeechTimeoutMs: Long? = null,
    val progress: ListenProgress = ListenProgress { },
)

/**
 * 2단계 듣기 (기술서 7-1). SDK의 끝점 판정을 기본으로 쓰고,
 * 부분 결과가 1.5초 동안 바뀌지 않으면 그 문장으로 끝낸다. 최대 15초.
 * 확인·추천·리마인드·방문 메모의 대답 듣기도 이 단계를 쓴다.
 */
class ListenStage @Inject constructor(
    private val recognizer: SpeechRecognizer,
    private val clock: Clock,
) : Stage<ListenRequest, ListenOutcome> {
    override val name = "2 Listen"

    override suspend fun run(input: ListenRequest): ListenOutcome = coroutineScope {
        val start = clock.now()
        var partial = ""
        var lastChange = start
        var final: String? = null
        var failure: SttFailure? = null
        val job = launch {
            recognizer.listen()
                .transformWhile { e -> emit(e); e !is SttEvent.Final && e !is SttEvent.Failed }
                .collect { e ->
                    input.progress.onEvent(e)
                    when (e) {
                        is SttEvent.Partial -> if (e.text != partial) {
                            partial = e.text
                            lastChange = clock.now()
                        }
                        is SttEvent.Final -> final = e.text
                        is SttEvent.Failed -> failure = e.reason
                        else -> Unit
                    }
                }
        }
        while (job.isActive) {
            val now = clock.now()
            val silent = partial.isNotBlank() && now - lastChange >= Tunables.LISTEN_SILENCE_MS
            val noSpeech = partial.isBlank() && input.noSpeechTimeoutMs?.let { now - start >= it } == true
            if (silent || noSpeech || now - start >= input.maxMs) break
            delay(POLL_MS)
        }
        job.cancel()
        val text = (final ?: partial).trim()
        when {
            text.isNotEmpty() -> ListenOutcome.Heard(text)
            failure != null && failure != SttFailure.NO_SPEECH -> ListenOutcome.Failed(failure!!)
            else -> ListenOutcome.Empty
        }
    }

    private companion object {
        const val POLL_MS = 50L
    }
}
