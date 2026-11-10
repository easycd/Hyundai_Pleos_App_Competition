package com.easycd.nextplace.feature.settings.ui

sealed interface SettingsAction {
    data class Remind(val on: Boolean) : SettingsAction
    data class Radius(val meters: Int) : SettingsAction
    data class MealOnly(val on: Boolean) : SettingsAction
    data class Volume(val value: Float) : SettingsAction
    data class Lookback(val seconds: Int) : SettingsAction
    data object DeleteAll : SettingsAction
    data object ConfirmDeleteAll : SettingsAction
    data object CancelDeleteAll : SettingsAction
    data object Back : SettingsAction
}
