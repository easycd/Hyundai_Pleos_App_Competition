package com.easycd.nextplace.core.ui.notify

import com.easycd.nextplace.core.domain.Tunables
import com.easycd.nextplace.core.domain.contract.RouteNotice
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

/** S-05b 확인 문구를 차량 알림으로 1.5초 띄운다. */
class HeadsUpRouteNotice @Inject constructor(private val headsUp: HeadsUp) : RouteNotice {
    override fun show(name: String, changing: Boolean) = headsUp.showRoute(name, changing, Tunables.RECOMMEND_SEND_DELAY_MS)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NotifyModule {
    @Binds abstract fun routeNotice(impl: HeadsUpRouteNotice): RouteNotice
}
