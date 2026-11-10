package com.easycd.nextplace

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.pleos.system.AndroidPermissionChecker
import com.easycd.nextplace.core.ui.component.ToastHost
import com.easycd.nextplace.core.ui.drive.LocalDriveRestricted
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.drive.LocalSpeak
import com.easycd.nextplace.core.ui.drive.isRestricted
import com.easycd.nextplace.core.ui.theme.LocalReduceMotion
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.core.ui.theme.NextPlaceTheme
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.core.ui.theme.NpType
import com.easycd.nextplace.feature.remind.ui.RemindRoute
import com.easycd.nextplace.feature.visit.ui.VisitNoteRoute
import com.easycd.nextplace.feature.voicesave.ui.VoiceOverlayHost
import com.easycd.nextplace.navigation.AppNavGraph
import com.easycd.nextplace.start.OnboardingScreen
import com.easycd.nextplace.start.PermissionDeniedScreen
import com.easycd.nextplace.start.SplashScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * 창 1개 (기술서 2장). 테마(차량 주·야간), 주행 제한, 세로 창 여부, 말하기 연결을 내려 주고
 * 화면 위에 음성 저장 겹침 · 리마인드 · 방문 메모 카드 · 권한 없음 창 · 토스트를 한 번씩 올린다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    /** S-13 시작하기: 실행 중 요청이 필요한 권한만 마이크 → 위치 → 내비 → 알림 순서로 창이 뜬다 (선언만으로 되는 권한은 창 없이 지나감). */
    private val askPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { vm.onOnboardingDone() }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener { it.remove() }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleLink(intent)
        val reduceMotion = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        setContent {
            CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
                NextPlaceTheme { Root() }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleLink(intent)
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    /** 공유 링크로 열림 → S-16 가져오기. 링크 데이터는 # 뒤에 있어 서버로 가지 않는다. */
    private fun handleLink(intent: Intent?) {
        val data = intent?.takeIf { it.action == Intent.ACTION_VIEW }?.dataString ?: return
        vm.navigator.go(Destination.Import(data))
    }

    private fun requestOnboardingPermissions() {
        val list = buildList {
            add(AndroidPermissionChecker.STT)
            add(AndroidPermissionChecker.TTS)
            add(AndroidPermissionChecker.LOCATION)
            add(AndroidPermissionChecker.NAVI)
            add(AndroidPermissionChecker.NAVI_SEARCH)
            add(AndroidPermissionChecker.NAVI_MAP)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
        }.filter { checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
        if (list.isEmpty()) vm.onOnboardingDone() else runCatching { askPermissions.launch(list.toTypedArray()) }.onFailure { vm.onOnboardingDone() }
    }

    /** S-17 설정에서 허용: 이 앱의 앱 정보(권한) 화면. "다시 묻지 않음" 상태여도 같은 화면. */
    private fun openAppSettings() {
        vm.closeDenied()
        runCatching {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
        }
    }

    @Composable
    private fun Root() {
        val state by vm.state.collectAsStateWithLifecycle()
        val restricted by vm.ux.restricted.collectAsStateWithLifecycle()
        val nav = rememberNavController()
        BoxWithConstraints(Modifier.fillMaxSize().background(Np.colors.background)) {
            val portrait = maxHeight > maxWidth
            CompositionLocalProvider(
                LocalDriveRestricted provides restricted,
                LocalPortrait provides portrait,
                LocalSpeak provides vm::speak,
            ) {
                if (state.phase != ShellPhase.SPLASH) {
                    AppNavGraph(nav, vm.navigator, vm::onPermissionNeeded, Modifier.fillMaxSize())
                    VoiceOverlayHost()
                    RemindRoute()
                    VisitNoteRoute()
                    OnboardingScreen(
                        visible = state.phase == ShellPhase.ONBOARDING,
                        onStart = ::requestOnboardingPermissions,
                        onLater = vm::onOnboardingDone,
                    )
                    PermissionDeniedScreen(
                        kind = state.denied,
                        restricted = restricted.isRestricted(),
                        canSaveLocationOnly = state.canSaveLocationOnly,
                        onOpenSettings = ::openAppSettings,
                        onClose = vm::closeDenied,
                        onSaveLocationOnly = vm::saveLocationOnly,
                    )
                    ToastHost(
                        vm.messages.messages,
                        bottomPadding = if (portrait) PORTRAIT_TOAST_BOTTOM else TOAST_BOTTOM,
                        sidePadding = if (portrait) PORTRAIT_TOAST_SIDE else TOAST_SIDE,
                    )
                    if (vm.simulatedLocation()) {
                        Text(
                            stringResource(R.string.sim_location),
                            style = NpType.small,
                            color = Np.colors.muted,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                        )
                    }
                }
                AnimatedVisibility(
                    state.phase == ShellPhase.SPLASH,
                    enter = fadeIn(tween(0)),
                    exit = fadeOut(tween(Motion.BASE_MS)),
                ) { SplashScreen(onDone = vm::onSplashDone) }
            }
        }
    }

    private companion object {
        // 토스트: 위에서 900dp, 높이 120dp, 좌우 140dp (1080dp 기준) · 세로 창은 말하기 버튼 위
        val TOAST_BOTTOM = 60.dp
        val TOAST_SIDE = 140.dp
        val PORTRAIT_TOAST_BOTTOM = 320.dp
        val PORTRAIT_TOAST_SIDE = 32.dp
    }
}
