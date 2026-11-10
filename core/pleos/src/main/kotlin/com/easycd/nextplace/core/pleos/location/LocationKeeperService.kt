package com.easycd.nextplace.core.pleos.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import com.easycd.nextplace.core.pleos.R

/**
 * 위치 포그라운드 서비스 (기술서 7-4 5). 리마인드·방문 판정이 앱이 뒤에 있어도 돌도록 프로세스를 살려 둔다.
 * 위치 콜백 자체는 [LocationTracker]가 받는다. Pleos 가 막으면 앱이 켜져 있을 때만 판정한다 (기획서 10장 20번).
 */
class LocationKeeperService : LifecycleService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        runCatching { ServiceCompat.startForeground(this, ID, notification(), type) }
            .onFailure { stopSelf() }
        return START_STICKY
    }

    private fun notification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, getString(R.string.keeper_channel), NotificationManager.IMPORTANCE_MIN))
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(getString(R.string.keeper_title))
            .setContentText(getString(R.string.keeper_text))
            .setOngoing(true)
            .build()
    }

    private companion object {
        const val CHANNEL = "location_keeper"
        const val ID = 41
    }
}
