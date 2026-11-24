
package com.vathos.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vathos.app.domain.usecase.GetAllNotesUseCase
import com.vathos.app.domain.usecase.GetAllTasksUseCase
import com.vathos.app.domain.usecase.GetConversationCountUseCase
import com.vathos.app.domain.usecase.GetDocumentsUseCase

class HomeViewModelFactory(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val getConversationCountUseCase: GetConversationCountUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                getDocumentsUseCase,
                getAllNotesUseCase,
                getAllTasksUseCase,
                getConversationCountUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
