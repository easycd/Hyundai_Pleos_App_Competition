package com.easycd.nextplace.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.easycd.nextplace.core.domain.model.AppSettings
import com.easycd.nextplace.core.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.store: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** 설정 (기술서 8장: Preferences DataStore, 키 7개). */
internal class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
) : SettingsRepository {
    private val store = context.store

    override val settings: Flow<AppSettings> = store.data.map { p ->
        val d = AppSettings()
        AppSettings(
            remindEnabled = p[REMIND] ?: d.remindEnabled,
            remindRadiusM = p[RADIUS] ?: d.remindRadiusM,
            mealTimeOnly = p[MEAL] ?: d.mealTimeOnly,
            voiceVolume = p[VOLUME] ?: d.voiceVolume,
            lookbackSec = p[LOOKBACK] ?: d.lookbackSec,
            lastRemindAt = p[LAST_REMIND],
            onboardingDone = p[ONBOARDED] ?: d.onboardingDone,
        )
    }

    override suspend fun current(): AppSettings = settings.first()

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        store.edit { p ->
            val now = AppSettings(
                remindEnabled = p[REMIND] ?: true,
                remindRadiusM = p[RADIUS] ?: AppSettings().remindRadiusM,
                mealTimeOnly = p[MEAL] ?: true,
                voiceVolume = p[VOLUME] ?: AppSettings.DEFAULT_VOLUME,
                lookbackSec = p[LOOKBACK] ?: AppSettings().lookbackSec,
                lastRemindAt = p[LAST_REMIND],
                onboardingDone = p[ONBOARDED] ?: false,
            )
            val next = transform(now)
            p[REMIND] = next.remindEnabled
            p[RADIUS] = next.remindRadiusM
            p[MEAL] = next.mealTimeOnly
            p[VOLUME] = next.voiceVolume
            p[LOOKBACK] = next.lookbackSec
            val last = next.lastRemindAt
            if (last == null) p.remove(LAST_REMIND) else p[LAST_REMIND] = last
            p[ONBOARDED] = next.onboardingDone
        }
    }

    private companion object {
        val REMIND = booleanPreferencesKey("remind_enabled")
        val RADIUS = intPreferencesKey("remind_radius_m")
        val MEAL = booleanPreferencesKey("meal_time_only")
        val VOLUME = floatPreferencesKey("voice_volume")
        val LOOKBACK = intPreferencesKey("lookback_sec")
        val LAST_REMIND = longPreferencesKey("last_remind_at")
        val ONBOARDED = booleanPreferencesKey("onboarding_done")
    }
}
