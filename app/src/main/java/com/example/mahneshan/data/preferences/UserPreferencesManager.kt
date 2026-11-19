package com.example.mahneshan.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppMode(val title: String) {
    TRACKING("ردیابی چرخه"),
    PREGNANCY("دوران بارداری")
}

private val Context.dataStore by preferencesDataStore(name = "mahneshan_prefs")

class UserPreferencesManager(private val context: Context) {

    companion object {
        val APP_MODE_KEY = stringPreferencesKey("app_mode")
        val DEFAULT_CYCLE_LENGTH = intPreferencesKey("default_cycle_length")
        val DEFAULT_PERIOD_LENGTH = intPreferencesKey("default_period_length")
        val PAIN_SEVERITY_KEY = stringPreferencesKey("pain_severity")
        val FLOW_INTENSITY_KEY = stringPreferencesKey("flow_intensity")
        val HAS_PREGNANCY_HISTORY_KEY = booleanPreferencesKey("has_pregnancy_history")
        val TAKES_HORMONAL_PILLS_KEY = booleanPreferencesKey("takes_hormonal_pills")
        val IS_ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("is_onboarding_completed")
    }

    val appMode: Flow<AppMode> = context.dataStore.data.map { prefs ->
        val modeName = prefs[APP_MODE_KEY] ?: AppMode.TRACKING.name
        AppMode.entries.find { it.name == modeName } ?: AppMode.TRACKING
    }

    val defaultCycleLength: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DEFAULT_CYCLE_LENGTH] ?: 28
    }

    val defaultPeriodLength: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DEFAULT_PERIOD_LENGTH] ?: 6
    }

    val painSeverity: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PAIN_SEVERITY_KEY] ?: "متوسط"
    }

    val flowIntensity: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[FLOW_INTENSITY_KEY] ?: "متوسط"
    }

    val hasPregnancyHistory: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAS_PREGNANCY_HISTORY_KEY] ?: false
    }

    val takesHormonalPills: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[TAKES_HORMONAL_PILLS_KEY] ?: false
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_ONBOARDING_COMPLETED_KEY] ?: false
    }

    suspend fun setAppMode(mode: AppMode) {
        context.dataStore.edit { it[APP_MODE_KEY] = mode.name }
    }

    suspend fun updateCycleSettings(cycleLength: Int, periodLength: Int) {
        context.dataStore.edit {
            it[DEFAULT_CYCLE_LENGTH] = cycleLength
            it[DEFAULT_PERIOD_LENGTH] = periodLength
        }
    }

    suspend fun saveOnboardingProfile(
        painSeverity: String,
        flowIntensity: String,
        periodLength: Int,
        hasPregnancyHistory: Boolean,
        takesHormonalPills: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[PAIN_SEVERITY_KEY] = painSeverity
            prefs[FLOW_INTENSITY_KEY] = flowIntensity
            prefs[DEFAULT_PERIOD_LENGTH] = periodLength
            prefs[HAS_PREGNANCY_HISTORY_KEY] = hasPregnancyHistory
            prefs[TAKES_HORMONAL_PILLS_KEY] = takesHormonalPills
            prefs[IS_ONBOARDING_COMPLETED_KEY] = true
        }
    }
}
