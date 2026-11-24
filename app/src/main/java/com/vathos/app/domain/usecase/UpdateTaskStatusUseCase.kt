
package com.vathos.app.domain.usecase

import com.vathos.app.domain.repository.TaskRepository

class UpdateTaskStatusUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(
        taskId: Long,
        completed: Boolean
    ) {
        require(taskId > 0) {
            "Invalid task ID"
        }

        repository.updateTaskStatus(taskId, completed)
    }
}
