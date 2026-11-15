package com.easycd.nextplace.core.ui.notify

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** 차량 알림 버튼을 받아 [HeadsUpAnswers] 로 넘기고 알림을 닫는다. 앱 안에서만 부른다 (exported = false). */
@AndroidEntryPoint
class HeadsUpActionReceiver : BroadcastReceiver() {
    @Inject lateinit var answers: HeadsUpAnswers

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
        val id = intent.getIntExtra(EXTRA_ID, -1).takeIf { it >= 0 } ?: return
        answers.emit(HeadsUpAnswer(id, intent.getBooleanExtra(EXTRA_POSITIVE, false)))
        context.getSystemService(NotificationManager::class.java)?.cancel(id)
    }

    companion object {
        const val ACTION = "com.easycd.nextplace.HEADS_UP_ANSWER"
        const val EXTRA_ID = "id"
        const val EXTRA_POSITIVE = "positive"
    }
}
