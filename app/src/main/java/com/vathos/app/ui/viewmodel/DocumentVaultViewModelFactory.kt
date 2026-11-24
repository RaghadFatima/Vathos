
package com.vathos.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vathos.app.domain.usecase.DeleteDocumentUseCase
import com.vathos.app.domain.usecase.GenerateDocumentSummaryUseCase
import com.vathos.app.domain.usecase.GetDocumentsUseCase
import com.vathos.app.domain.usecase.ImportPdfUseCase
import com.vathos.app.domain.usecase.SearchDocumentsUseCase

class DocumentVaultViewModelFactory(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val searchDocumentsUseCase: SearchDocumentsUseCase,
    private val importPdfUseCase: ImportPdfUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val generateSummaryUseCase: GenerateDocumentSummaryUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentVaultViewModel::class.java)) {
            return DocumentVaultViewModel(
                getDocumentsUseCase,
                searchDocumentsUseCase,
                importPdfUseCase,
                deleteDocumentUseCase,
                generateSummaryUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
