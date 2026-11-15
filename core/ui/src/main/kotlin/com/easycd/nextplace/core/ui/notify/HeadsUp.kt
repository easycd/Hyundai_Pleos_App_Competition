package com.easycd.nextplace.core.ui.notify

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import com.easycd.nextplace.core.domain.voice.Josa
import com.easycd.nextplace.core.ui.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** 알림 카드의 버튼 두 개 (화면 설계서 1장 버튼 순서: 취소·닫기는 왼쪽, 실행은 오른쪽). */
data class HeadsUpButtons(val negative: String, val positive: String)

/**
 * 차량 알림(heads-up) (화면 설계서 4장, 기술서 10장 '권한과 앱 밖 알림'): 높은 중요도 + 버튼 2개.
 * 리마인드 S-05 · 방문 메모 S-15 를 내비 화면 위에 띄우고, 버튼을 누르면 [HeadsUpAnswers] 로 대답이 간다.
 * 안내 시작 확인 문구(S-05b · S-06c · S-08)는 버튼 없이 1.5초 동안 띄운다 (내비가 화면을 넘겨받아 앱 카드가 안 보이므로).
 * 차량에서 heads-up 이 안 뜨면 앱이 켜져 있을 때만 앱 창 카드 + TTS 로 동작한다 (기획서 10장 20번).
 */
@Singleton
class HeadsUp @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    init {
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, context.getString(R.string.headsup_channel), NotificationManager.IMPORTANCE_HIGH).apply { setSound(null, null) },
        )
    }

    @SuppressLint("MissingPermission")
    fun show(id: Int, title: String, text: String, timeoutMs: Long, buttons: HeadsUpButtons? = null) {
        val n = builder(id, title, text, timeoutMs)
            .apply {
                if (buttons != null) {
                    addAction(action(id, buttons.negative, positive = false))
                    addAction(action(id, buttons.positive, positive = true))
                }
            }
            .build()
        runCatching { manager.notify(id, n) }
    }

    /** 안내 시작 확인 문구 "서울갈비로 안내할게요" / "목적지를 서울갈비로 바꿀게요" (S-05b: 1.5초 뒤 사라짐). */
    fun showRoute(name: String, changing: Boolean, timeoutMs: Long) {
        val text = context.getString(if (changing) R.string.toast_route_change else R.string.toast_route, Josa.ro(name))
        show(ROUTE_ID, context.getString(R.string.headsup_route_title), text, timeoutMs)
    }

    fun cancel(id: Int) = manager.cancel(id)

    private fun builder(id: Int, title: String, text: String, timeoutMs: Long): Notification.Builder {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        val pi = launch?.let { PendingIntent.getActivity(context, id, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT) }
        return Notification.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle(title)
            .setContentText(text)
            .setCategory(Notification.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setTimeoutAfter(timeoutMs)
            .apply { if (pi != null) setContentIntent(pi) }
    }

    private fun action(id: Int, label: String, positive: Boolean): Notification.Action {
        val intent = Intent(context, HeadsUpActionReceiver::class.java)
            .setAction(HeadsUpActionReceiver.ACTION)
            .putExtra(HeadsUpActionReceiver.EXTRA_ID, id)
            .putExtra(HeadsUpActionReceiver.EXTRA_POSITIVE, positive)
        val request = id * 2 + if (positive) 1 else 0
        val pi = PendingIntent.getBroadcast(context, request, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return Notification.Action.Builder(null as Icon?, label, pi).build()
    }

    companion object {
        const val REMIND_ID = 501
        const val VISIT_ID = 502
        const val ROUTE_ID = 503
        private const val CHANNEL = "nearby"
    }
}
