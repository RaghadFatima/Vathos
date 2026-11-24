
package com.vathos.app.domain.usecase

import com.vathos.app.domain.model.Task
import com.vathos.app.domain.repository.TaskRepository

class CreateTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(
        title: String,
        dueDate: String
    ): Long {
        require(title.isNotBlank()) {
            "Task title cannot be empty"
        }

        require(dueDate.isNotBlank()) {
            "Task due date cannot be empty"
        }

        val task = Task(
            id = 0,
            title = title.trim(),
            dueDate = dueDate.trim(),
            isCompleted = false,
            createdAt = System.currentTimeMillis()
        )

        return repository.createTask(task)
    }
}
