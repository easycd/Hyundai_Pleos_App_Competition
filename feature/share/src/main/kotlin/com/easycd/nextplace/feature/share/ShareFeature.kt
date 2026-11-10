package com.easycd.nextplace.feature.share

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.easycd.nextplace.core.ui.nav.Feature
import com.easycd.nextplace.core.ui.nav.Routes
import com.easycd.nextplace.feature.share.ui.receive.ImportRoute
import com.easycd.nextplace.feature.share.ui.send.ShareRoute

/** F-13 등록: S-10 공유, S-16 가져오기. */
object ShareFeature : Feature {
    override val name = "share"
    override fun register(graph: NavGraphBuilder) {
        graph.composable(Routes.SHARE, arguments = listOf(navArgument(Routes.ARG_PRE) { type = NavType.StringType; nullable = true })) { ShareRoute() }
        graph.composable(Routes.IMPORT, arguments = listOf(navArgument(Routes.ARG_PAYLOAD) { type = NavType.StringType; defaultValue = "" })) { ImportRoute() }
    }
}
