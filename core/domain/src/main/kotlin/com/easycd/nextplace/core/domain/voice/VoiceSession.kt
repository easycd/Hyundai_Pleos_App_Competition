package com.easycd.nextplace.core.domain.voice

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 누가 지금 마이크와 스피커를 쓰는지 한 곳에서 정한다 (기술서 7-0).
 * 음성 작업은 한 번에 하나. 사용자가 시작한 것(말하기 버튼)은 진행 중인 작업을 끝내고 바로 시작하고,
 * 리마인드·방문 메모는 줄을 서서 기다린다 (방문 메모가 리마인드보다 먼저, 리마인드는 30초 넘으면 버림).
 */
@Singleton
class VoiceSession @Inject constructor() {

    enum class Kind { USER, VISIT_NOTE, REMIND }

    /** 끝낸 쪽이 미확인 저장으로 마무리하도록 알리는 취소 사유. */
    class Preempted : CancellationException("preempted by a new voice request")

    private class Owner(val kind: Kind, val job: Job?, val id: Long)

    private val mutex = Mutex()
    private val ids = AtomicLong()
    private val waitingNotes = AtomicInteger()
    @Volatile private var owner: Owner? = null
    private val _active = MutableStateFlow<Kind?>(null)

    /** 지금 진행 중인 음성 작업 종류 (없으면 null). */
    val active: StateFlow<Kind?> = _active.asStateFlow()

    /** 사용자가 시작한 작업. 진행 중인 작업이 있으면 취소(Preempted)하고 끝나기를 기다린 뒤 시작한다. */
    suspend fun <T> runUser(block: suspend (sessionId: Long) -> T): T {
        owner?.job?.let { job ->
            job.cancel(Preempted())
            job.join()
        }
        mutex.lock()
        return runLocked(Kind.USER, block)
    }

    /**
     * 줄 서는 작업. [maxWaitMs] 안에 차례가 오지 않으면 null (실행 안 함).
     * 우선순위(기술서 7-0): 방문 메모가 기다리고 있으면 리마인드는 차례가 와도 양보하고 다시 줄을 선다.
     */
    suspend fun <T> runQueued(kind: Kind, maxWaitMs: Long, block: suspend (sessionId: Long) -> T): T? {
        if (kind == Kind.VISIT_NOTE) waitingNotes.incrementAndGet()
        val got = try {
            withTimeoutOrNull(maxWaitMs) { lockQueued(kind) }
        } finally {
            if (kind == Kind.VISIT_NOTE) waitingNotes.decrementAndGet()
        }
        got ?: return null
        return runLocked(kind, block)
    }

    private suspend fun lockQueued(kind: Kind) {
        while (true) {
            mutex.lock()
            if (kind != Kind.REMIND || waitingNotes.get() == 0) return
            // 줄 선 방문 메모에게 먼저 넘긴다 (Mutex 는 먼저 온 순서라 방문 메모가 다음 차례가 된다)
            mutex.unlock()
            yield()
        }
    }

    fun isBusy(): Boolean = mutex.isLocked

    private suspend fun <T> runLocked(kind: Kind, block: suspend (Long) -> T): T {
        val id = ids.incrementAndGet()
        owner = Owner(kind, currentCoroutineContext()[Job], id)
        _active.value = kind
        try {
            return block(id)
        } finally {
            owner = null
            _active.value = null
            mutex.unlock()
        }
    }
}
