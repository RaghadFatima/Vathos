
package com.vathos.app.domain.usecase

import com.vathos.app.domain.repository.TaskRepository

class DeleteAllTasksUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke() {
        repository.deleteAllTasks()
    }
}
