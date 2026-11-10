package com.easycd.nextplace.feature.recommend.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RecommendRoute(vm: RecommendViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    BackHandler { vm.onAction(RecommendAction.Back) }
    RecommendScreen(state, vm::onAction)
}
