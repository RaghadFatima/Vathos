
package com.vathos.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vathos.app.domain.model.Document
import com.vathos.app.domain.usecase.DeleteDocumentUseCase
import com.vathos.app.domain.usecase.GenerateDocumentSummaryUseCase
import com.vathos.app.domain.usecase.GetDocumentsUseCase
import com.vathos.app.domain.usecase.ImportPdfUseCase
import com.vathos.app.domain.usecase.SearchDocumentsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DocumentVaultViewModel(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val searchDocumentsUseCase: SearchDocumentsUseCase,
    private val importPdfUseCase: ImportPdfUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val generateSummaryUseCase: GenerateDocumentSummaryUseCase
) : ViewModel() {

    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedSummary = MutableStateFlow<String?>(null)
    val selectedSummary: StateFlow<String?> = _selectedSummary.asStateFlow()

    private var searchJob: Job? = null

    init {
        searchDocuments("")
    }

    fun searchDocuments(query: String) {
        _searchQuery.value = query

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchDocumentsUseCase(query).collect { result ->
                _documents.value = result
            }
        }
    }

    fun importPdf(uriString: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                importPdfUseCase(uriString)
            } catch (exception: Exception) {
                _error.value = exception.message ?: "Could not import PDF"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteDocument(documentId: Long) {
        viewModelScope.launch {
            try {
                deleteDocumentUseCase(documentId)
            } catch (exception: Exception) {
                _error.value = exception.message ?: "Could not delete document"
            }
        }
    }

    fun generateSummary(documentId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                _selectedSummary.value =
                    generateSummaryUseCase(documentId)
            } catch (exception: Exception) {
                _error.value = exception.message ?: "Could not generate summary"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
