package com.easycd.nextplace.feature.voicesave.recovery

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 미확인 가게 재검색 예약 (기술서 10장 '네트워크 없음·POI 실패'). 네트워크가 연결되면 1번 돈다.
 * 같은 이름의 작업은 하나만 둔다 (KEEP): 한 번 돌면 후보 없는 미확인 가게를 전부 다시 찾는다.
 */
@Singleton
class ResearchScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    fun schedule() {
        val request = OneTimeWorkRequestBuilder<ResearchWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SEC, TimeUnit.SECONDS)
            .addTag(TAG)
            .build()
        runCatching { WorkManager.getInstance(context).enqueueUniqueWork(NAME, ExistingWorkPolicy.KEEP, request) }
    }

    /** 전체 삭제(S-11b) 때 작업도 함께 해제. */
    fun cancelAll() {
        runCatching { WorkManager.getInstance(context).cancelAllWorkByTag(TAG) }
    }

    private companion object {
        const val NAME = "research-unconfirmed"
        const val TAG = "research"
        const val BACKOFF_SEC = 30L
    }
}
