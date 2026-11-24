
package com.vathos.app.domain.usecase

import com.vathos.app.domain.model.Task
import com.vathos.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetPendingTasksUseCase(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return repository.getPendingTasks()
    }
}
