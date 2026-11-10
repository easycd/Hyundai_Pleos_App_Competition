package com.easycd.nextplace.core.pleos.system

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.easycd.nextplace.core.domain.sdk.PermissionChecker
import com.easycd.nextplace.core.pleos.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** 권한 상태. 가짜 SDK 빌드는 Pleos 권한이 없는 일반 에뮬레이터에서도 돌도록 허용으로 본다. */
class AndroidPermissionChecker @Inject constructor(@ApplicationContext private val context: Context) : PermissionChecker {
    override fun hasMicrophone() = fake || granted(STT)
    override fun hasLocation() = fake || granted(LOCATION)
    override fun hasNavi() = fake || granted(NAVI)
    override fun hasNotification() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || granted(android.Manifest.permission.POST_NOTIFICATIONS)

    private val fake get() = BuildConfig.FAKE_SDK
    private fun granted(p: String) = context.checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val STT = "pleos.car.permission.STT_SERVICE"
        const val TTS = "pleos.car.permission.TTS_SERVICE"
        const val LOCATION = "pleos.car.permission.FUSED_LOCATION"
        const val NAVI = "pleos.car.permission.NAVI_ROUTE"
        const val NAVI_SEARCH = "pleos.car.permission.NAVI_ROUTE_SEARCH"
        const val NAVI_MAP = "pleos.car.permission.NAVI_CUSTOM_MAP"
    }
}
