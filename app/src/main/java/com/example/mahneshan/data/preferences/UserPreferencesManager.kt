package com.example.mahneshan.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppMode(val title: String) {
    TRACKING("پیگیری و پیش‌گیری"),
    TRYING_CONCEIVE("اقدام به بارداری"),
    PREGNANCY("دوران بارداری")
}

private val Context.dataStore by preferencesDataStore(name = "mahneshan_prefs")

class UserPreferencesManager(private val context: Context) {

    companion object {
        val APP_MODE_KEY = stringPreferencesKey("app_mode")
        val DEFAULT_CYCLE_LENGTH = intPreferencesKey("default_cycle_length")
        val DEFAULT_PERIOD_LENGTH = intPreferencesKey("default_period_length")
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

    suspend fun setAppMode(mode: AppMode) {
        context.dataStore.edit { it[APP_MODE_KEY] = mode.name }
    }

    suspend fun updateCycleSettings(cycleLength: Int, periodLength: Int) {
        context.dataStore.edit {
            it[DEFAULT_CYCLE_LENGTH] = cycleLength
            it[DEFAULT_PERIOD_LENGTH] = periodLength
        }
    }
}
