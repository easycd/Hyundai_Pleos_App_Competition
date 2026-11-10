package com.easycd.nextplace.feature.myplaces

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.easycd.nextplace.core.ui.nav.Feature
import com.easycd.nextplace.core.ui.nav.Routes
import com.easycd.nextplace.feature.myplaces.ui.detail.PlaceDetailRoute
import com.easycd.nextplace.feature.myplaces.ui.edit.PlaceEditRoute
import com.easycd.nextplace.feature.myplaces.ui.list.MyPlacesRoute
import com.easycd.nextplace.feature.myplaces.ui.resolve.ResolveRoute
import com.easycd.nextplace.feature.myplaces.ui.search.DirectSearchRoute

/** F-07 · F-12 등록: S-07 ~ S-09b. 모두 정차 전용(DriveGate). */
object MyPlacesFeature : Feature {
    override val name = "myplaces"
    override fun register(graph: NavGraphBuilder) {
        graph.composable(Routes.MY_PLACES) { MyPlacesRoute() }
        graph.composable(Routes.PLACE) { PlaceDetailRoute() }
        graph.composable(Routes.EDIT) { PlaceEditRoute() }
        graph.composable(Routes.RESOLVE) { ResolveRoute() }
        graph.composable(Routes.SEARCH) { DirectSearchRoute() }
    }
}
