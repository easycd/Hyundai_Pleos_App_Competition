package com.easycd.nextplace.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.easycd.nextplace.core.domain.contract.Destination
import com.easycd.nextplace.core.domain.contract.Navigator
import com.easycd.nextplace.core.domain.contract.PermissionKind
import com.easycd.nextplace.core.ui.nav.Routes
import com.easycd.nextplace.core.ui.theme.Motion
import com.easycd.nextplace.registry.FeatureRegistry

/** 화면 등록표 조립 (기술서 6장): 기능 등록표의 화면을 그래프에 올리고, Navigator 요청을 NavController 로 옮긴다. */
@Composable
fun AppNavGraph(
    nav: NavHostController,
    navigator: Navigator,
    onPermissionNeeded: (PermissionKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(nav, navigator) {
        navigator.commands.collect { d ->
            if (d is Destination.PermissionNeeded) onPermissionNeeded(d.kind) else nav.go(d)
        }
    }
    NavHost(
        navController = nav,
        startDestination = Routes.HOME,
        modifier = modifier,
        enterTransition = { fadeIn(tween(Motion.BASE_MS, easing = Motion.standard)) },
        exitTransition = { fadeOut(tween(Motion.FAST_MS)) },
        popEnterTransition = { fadeIn(tween(Motion.BASE_MS, easing = Motion.standard)) },
        popExitTransition = { fadeOut(tween(Motion.FAST_MS)) },
    ) {
        FeatureRegistry.features.forEach { it.register(this) }
    }
}

/** 이동 규칙: 같은 화면은 겹쳐 쌓지 않고, 홈·내 맛집은 그 아래를 비운다. 후보 고르기·직접 검색 뒤 상세로 가면 그 둘을 걷어 낸다. */
private fun NavHostController.go(d: Destination) {
    when (d) {
        Destination.Back -> popBackStack()
        Destination.Home -> navigate(Routes.HOME) {
            popUpTo(Routes.HOME) { inclusive = false }
            launchSingleTop = true
        }
        Destination.MyPlaces -> navigate(Routes.MY_PLACES) {
            popUpTo(Routes.HOME) { inclusive = false }
            launchSingleTop = true
        }
        is Destination.PlaceDetail -> {
            while (currentDestination?.route in TRANSIENT) popBackStack()
            val route = Routes.of(d) ?: return
            val top = currentBackStackEntry
            val same = top?.destination?.route == Routes.PLACE && top.arguments?.getString(Routes.ARG_ID) == d.id
            if (!same) navigate(route) { launchSingleTop = true }
        }
        else -> Routes.of(d)?.let { navigate(it) { launchSingleTop = true } }
    }
}

private val TRANSIENT = setOf(Routes.RESOLVE, Routes.SEARCH, Routes.EDIT)
