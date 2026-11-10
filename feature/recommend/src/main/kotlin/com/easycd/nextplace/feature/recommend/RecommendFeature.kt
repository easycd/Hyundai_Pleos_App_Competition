package com.easycd.nextplace.feature.recommend

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.easycd.nextplace.core.ui.nav.Feature
import com.easycd.nextplace.core.ui.nav.Routes
import com.easycd.nextplace.feature.recommend.ui.RecommendRoute

/** F-09 · F-10 등록: 추천 화면. */
object RecommendFeature : Feature {
    override val name = "recommend"
    override fun register(graph: NavGraphBuilder) {
        graph.composable(Routes.RECOMMEND) { RecommendRoute() }
    }
}
