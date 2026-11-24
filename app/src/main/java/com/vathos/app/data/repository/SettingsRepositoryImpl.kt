
package com.vathos.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vathos.app.data.AppTheme
import com.vathos.app.data.VathosPreferences
import com.vathos.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.vathosDataStore by preferencesDataStore(
    name = "vathos_preferences"
)

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private object Keys {
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val THEME = stringPreferencesKey("theme")
    }

    override val preferences: Flow<VathosPreferences> =
        context.vathosDataStore.data.map { preferences ->

            val themeName = preferences[Keys.THEME]
                ?: AppTheme.SYSTEM.name

            VathosPreferences(
                offlineMode = preferences[Keys.OFFLINE_MODE] ?: true,
                theme = runCatching {
                    AppTheme.valueOf(themeName)
                }.getOrDefault(AppTheme.SYSTEM)
            )
        }

    override suspend fun setOfflineMode(enabled: Boolean) {
        context.vathosDataStore.edit { preferences ->
            preferences[Keys.OFFLINE_MODE] = enabled
        }
    }

    override suspend fun setTheme(theme: AppTheme) {
        context.vathosDataStore.edit { preferences ->
            preferences[Keys.THEME] = theme.name
        }
    }
}
