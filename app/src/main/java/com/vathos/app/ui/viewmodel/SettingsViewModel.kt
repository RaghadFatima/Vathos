
package com.vathos.app.ui.viewmodel

import android.net.TrafficStats
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vathos.app.data.AppTheme
import com.vathos.app.data.VathosPreferences
import com.vathos.app.domain.repository.SettingsRepository
import com.vathos.app.domain.usecase.ClearAllChatsUseCase
import com.vathos.app.domain.usecase.DeleteAllDocumentsUseCase
import com.vathos.app.domain.usecase.ExportVathosDataUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NetworkUsageState(
    val sentBytes: Long? = null,
    val receivedBytes: Long? = null,
    val isSupported: Boolean = false
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val clearAllChatsUseCase: ClearAllChatsUseCase,
    private val deleteAllDocumentsUseCase: DeleteAllDocumentsUseCase,
    private val exportVathosDataUseCase: ExportVathosDataUseCase
) : ViewModel() {

    val preferences: StateFlow<VathosPreferences> =
        settingsRepository.preferences.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VathosPreferences()
        )

    private val _networkUsage =
        MutableStateFlow(NetworkUsageState())

    val networkUsage: StateFlow<NetworkUsageState> =
        _networkUsage.asStateFlow()

    private var baselineTxBytes: Long? = null
    private var baselineRxBytes: Long? = null

    init {
        refreshNetworkUsage()
    }

    fun refreshNetworkUsage() {
        val uid = Process.myUid()

        val currentTx = TrafficStats.getUidTxBytes(uid)
        val currentRx = TrafficStats.getUidRxBytes(uid)

        if (currentTx == -1L || currentRx == -1L) {
            _networkUsage.value = NetworkUsageState(
                isSupported = false
            )
            return
        }

        if (baselineTxBytes == null || baselineRxBytes == null) {
            baselineTxBytes = currentTx
            baselineRxBytes = currentRx
        }

        _networkUsage.value = NetworkUsageState(
            sentBytes = (currentTx - baselineTxBytes!!)
                .coerceAtLeast(0L),
            receivedBytes = (currentRx - baselineRxBytes!!)
                .coerceAtLeast(0L),
            isSupported = true
        )
    }

    private fun resetNetworkUsageBaseline() {
        val uid = Process.myUid()

        val currentTx = TrafficStats.getUidTxBytes(uid)
        val currentRx = TrafficStats.getUidRxBytes(uid)

        if (currentTx != -1L && currentRx != -1L) {
            baselineTxBytes = currentTx
            baselineRxBytes = currentRx

            _networkUsage.value = NetworkUsageState(
                sentBytes = 0L,
                receivedBytes = 0L,
                isSupported = true
            )
        } else {
            baselineTxBytes = null
            baselineRxBytes = null

            _networkUsage.value = NetworkUsageState(
                isSupported = false
            )
        }
    }

    fun setOfflineMode(enabled: Boolean) {
        if (enabled) {
            resetNetworkUsageBaseline()
        }

        viewModelScope.launch {
            settingsRepository.setOfflineMode(enabled)
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun clearAllChats(onComplete: (String?) -> Unit = {}) {
        viewModelScope.launch {
            try {
                clearAllChatsUseCase()
                onComplete(null)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                onComplete(
                    exception.message ?: "Could not clear chats"
                )
            }
        }
    }

    fun clearAllDocuments(onComplete: (String?) -> Unit = {}) {
        viewModelScope.launch {
            try {
                deleteAllDocumentsUseCase()
                onComplete(null)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                onComplete(
                    exception.message ?: "Could not clear documents"
                )
            }
        }
    }

    fun exportData(onComplete: (String?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val json = exportVathosDataUseCase()
                onComplete(json, null)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                onComplete(
                    null,
                    exception.message ?: "Could not export Vathos data"
                )
            }
        }
    }
}
