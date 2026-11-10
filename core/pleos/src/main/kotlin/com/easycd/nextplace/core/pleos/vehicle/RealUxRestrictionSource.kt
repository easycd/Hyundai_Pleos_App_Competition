package com.easycd.nextplace.core.pleos.vehicle

import android.car.Car
import android.car.drivingstate.CarUxRestrictionsManager
import android.content.Context
import com.easycd.nextplace.core.domain.sdk.UxRestrictionSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 주행 제한 (F-14, 기술서 8장). CarUxRestrictionsManager 의 isRequiresDistractionOptimization 을 그대로 쓴다.
 * 속도·브레이크로 직접 판단하지 않고, 값을 아직 못 받았으면 null(= 제한).
 */
@Singleton
class RealUxRestrictionSource @Inject constructor(@ApplicationContext context: Context) : UxRestrictionSource {
    private val _restricted = MutableStateFlow<Boolean?>(null)
    override val restricted: StateFlow<Boolean?> = _restricted.asStateFlow()

    init {
        runCatching {
            val car = Car.createCar(context)
            val manager = car.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as CarUxRestrictionsManager
            manager.registerListener { r -> _restricted.value = r.isRequiresDistractionOptimization }
            manager.currentCarUxRestrictions?.let { _restricted.value = it.isRequiresDistractionOptimization }
        }
    }
}
