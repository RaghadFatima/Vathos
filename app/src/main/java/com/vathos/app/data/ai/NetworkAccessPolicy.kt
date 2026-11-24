
package com.vathos.app.data.ai

import com.vathos.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class NetworkAccessPolicy(
    private val settingsRepository: SettingsRepository
) {
    suspend fun ensureNetworkAllowed() {
        val preferences = settingsRepository.preferences.first()

        if (preferences.offlineMode) {
            throw IllegalStateException(
                "Network access is blocked while Offline Mode is enabled."
            )
        }
    }
}
