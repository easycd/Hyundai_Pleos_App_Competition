package com.easycd.nextplace.core.domain.usecase

import com.easycd.nextplace.core.domain.Tunables
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** DB 쓰기 3회 재시도 (0.2·0.5·1초, 기술서 10장). 끝내 실패하면 Result.failure. */
suspend fun <T> withDbRetry(block: suspend () -> T): Result<T> {
    var last: Throwable? = null
    for (attempt in 0..Tunables.DB_RETRY_BACKOFF_MS.size) {
        try {
            return Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            last = e
            Tunables.DB_RETRY_BACKOFF_MS.getOrNull(attempt)?.let { delay(it) }
        }
    }
    return Result.failure(last ?: IllegalStateException("db"))
}
