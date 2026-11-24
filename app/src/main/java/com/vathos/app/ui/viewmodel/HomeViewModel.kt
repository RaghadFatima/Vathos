
package com.vathos.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vathos.app.domain.model.Document
import com.vathos.app.domain.model.Note
import com.vathos.app.domain.model.Task
import com.vathos.app.domain.usecase.GetAllNotesUseCase
import com.vathos.app.domain.usecase.GetAllTasksUseCase
import com.vathos.app.domain.usecase.GetConversationCountUseCase
import com.vathos.app.domain.usecase.GetDocumentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    getDocumentsUseCase: GetDocumentsUseCase,
    getAllNotesUseCase: GetAllNotesUseCase,
    getAllTasksUseCase: GetAllTasksUseCase,
    getConversationCountUseCase: GetConversationCountUseCase
) : ViewModel() {

    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _conversationCount = MutableStateFlow(0)
    val conversationCount: StateFlow<Int> = _conversationCount.asStateFlow()

    init {
        viewModelScope.launch {
            getDocumentsUseCase().collect {
                _documents.value = it
            }
        }

        viewModelScope.launch {
            getAllNotesUseCase().collect {
                _notes.value = it
            }
        }

        viewModelScope.launch {
            getAllTasksUseCase().collect {
                _tasks.value = it
            }
        }

        viewModelScope.launch {
            getConversationCountUseCase().collect {
                _conversationCount.value = it
            }
        }
    }
}
