package com.easycd.nextplace.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.easycd.nextplace.core.ui.nav.Feature
import com.easycd.nextplace.core.ui.nav.Routes
import com.easycd.nextplace.feature.settings.ui.SettingsRoute

/** F-15 등록: S-11 설정. */
object SettingsFeature : Feature {
    override val name = "settings"
    override fun register(graph: NavGraphBuilder) {
        graph.composable(Routes.SETTINGS) { SettingsRoute() }
    }
}
