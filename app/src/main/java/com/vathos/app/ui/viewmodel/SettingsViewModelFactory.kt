
package com.vathos.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vathos.app.domain.repository.SettingsRepository
import com.vathos.app.domain.usecase.ClearAllChatsUseCase
import com.vathos.app.domain.usecase.DeleteAllDocumentsUseCase
import com.vathos.app.domain.usecase.ExportVathosDataUseCase

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val clearAllChatsUseCase: ClearAllChatsUseCase,
    private val deleteAllDocumentsUseCase: DeleteAllDocumentsUseCase,
    private val exportVathosDataUseCase: ExportVathosDataUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                settingsRepository,
                clearAllChatsUseCase,
                deleteAllDocumentsUseCase,
                exportVathosDataUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
