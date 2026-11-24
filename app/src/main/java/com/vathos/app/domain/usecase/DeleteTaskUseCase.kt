
package com.vathos.app.domain.usecase

import com.vathos.app.domain.repository.TaskRepository

class DeleteTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long) {
        require(taskId > 0) {
            "Invalid task ID"
        }

        repository.deleteTask(taskId)
    }
}
