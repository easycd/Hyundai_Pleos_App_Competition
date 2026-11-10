package com.easycd.nextplace.feature.voicesave

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.easycd.nextplace.core.ui.nav.Feature
import com.easycd.nextplace.core.ui.nav.Routes
import com.easycd.nextplace.feature.voicesave.ui.home.HomeRoute

/** F-01 ~ F-06 등록: 홈 화면. 음성 레이어는 app 이 VoiceOverlayHost 로 올린다. */
object VoiceSaveFeature : Feature {
    override val name = "voicesave"
    override fun register(graph: NavGraphBuilder) {
        graph.composable(Routes.HOME) { HomeRoute() }
    }
}
