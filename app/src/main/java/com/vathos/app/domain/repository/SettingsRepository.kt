
package com.vathos.app.domain.repository

import com.vathos.app.data.AppTheme
import com.vathos.app.data.VathosPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val preferences: Flow<VathosPreferences>

    suspend fun setOfflineMode(enabled: Boolean)

    suspend fun setTheme(theme: AppTheme)
}
