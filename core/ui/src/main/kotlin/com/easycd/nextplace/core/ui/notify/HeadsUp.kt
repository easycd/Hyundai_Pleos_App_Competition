package com.easycd.nextplace.core.ui.notify

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 차량 알림(heads-up) (화면 설계서 4장): 리마인드 S-05 · 방문 메모 S-15 를 내비 화면 위에 띄운다.
 * 누르면 앱이 열린다. 대답은 음성(5초)으로 받고, 앱 창이 보이면 같은 카드를 앱 안에도 띄운다.
 * 차량에서 heads-up 이 안 뜨면 앱이 켜져 있을 때만 앱 창 카드 + TTS 로 동작한다 (기획서 10장 20번).
 */
@Singleton
class HeadsUp @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    init {
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply { setSound(null, null) },
        )
    }

    @SuppressLint("MissingPermission")
    fun show(id: Int, title: String, text: String, timeoutMs: Long) {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        val pi = launch?.let { PendingIntent.getActivity(context, id, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT) }
        val n = Notification.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle(title)
            .setContentText(text)
            .setCategory(Notification.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setTimeoutAfter(timeoutMs)
            .apply { if (pi != null) setContentIntent(pi) }
            .build()
        runCatching { manager.notify(id, n) }
    }

    fun cancel(id: Int) = manager.cancel(id)

    companion object {
        const val REMIND_ID = 501
        const val VISIT_ID = 502
        const val ROUTE_ID = 503
        private const val CHANNEL = "nearby"
        private const val CHANNEL_NAME = "근처 알림"
    }
}
