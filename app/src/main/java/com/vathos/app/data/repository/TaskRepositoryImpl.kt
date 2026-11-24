
package com.vathos.app.data.repository

import com.vathos.app.data.local.dao.TaskDao
import com.vathos.app.data.local.entity.TaskEntity
import com.vathos.app.domain.model.Task
import com.vathos.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { tasks ->
            tasks.map { it.toDomain() }
        }
    }

    override fun getPendingTasks(): Flow<List<Task>> {
        return taskDao.getPendingTasks().map { tasks ->
            tasks.map { it.toDomain() }
        }
    }

    override suspend fun createTask(task: Task): Long {
        return taskDao.insertTask(
            TaskEntity(
                id = task.id,
                title = task.title,
                dueDate = task.dueDate,
                isCompleted = task.isCompleted,
                createdAt = task.createdAt
            )
        )
    }

    override suspend fun updateTaskStatus(
        taskId: Long,
        completed: Boolean
    ) {
        taskDao.updateTaskStatus(taskId, completed)
    }

    override suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTask(taskId)
    }

    override suspend fun deleteAllTasks() {
        taskDao.deleteAllTasks()
    }

    private fun TaskEntity.toDomain(): Task {
        return Task(
            id = id,
            title = title,
            dueDate = dueDate,
            isCompleted = isCompleted,
            createdAt = createdAt
        )
    }
}
