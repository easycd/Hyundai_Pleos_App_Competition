package com.easycd.nextplace.feature.myplaces.usecase

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.Messages
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.domain.repository.PlaceRepository
import com.easycd.nextplace.core.domain.sdk.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 삭제 뒤 되돌리기 (S-08d): 바로 '삭제 예정'으로 숨기고 4초 동안 되돌리기 토스트.
 * 4초 안에 다른 가게를 지우면 앞의 삭제는 바로 확정. 그 사이 앱이 꺼져도 다음 실행 때 확정 (purgeMarkedDeleted).
 */
@Singleton
class DeletePlaceUseCase @Inject constructor(
    private val places: PlaceRepository,
    private val messages: Messages,
    private val clock: Clock,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pending: Pair<String, Job>? = null

    fun delete(id: String, name: String) {
        scope.launch {
            pending?.let { (prevId, job) ->
                job.cancel()
                places.deleteNow(prevId)
            }
            places.markDeleted(id, clock.now())
            val job = scope.launch {
                delay(Tunables.UNDO_DELETE_MS)
                places.deleteNow(id)
                if (pending?.first == id) pending = null
            }
            pending = id to job
            messages.show(UserMessage.Deleted(name) { undo(id) })
        }
    }

    private fun undo(id: String) {
        scope.launch {
            val p = pending ?: return@launch
            if (p.first != id) return@launch
            p.second.cancel()
            pending = null
            places.restore(id)
        }
    }
}
