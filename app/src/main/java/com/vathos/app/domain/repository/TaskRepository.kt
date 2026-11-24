
package com.vathos.app.domain.repository

import com.vathos.app.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    fun getAllTasks(): Flow<List<Task>>

    fun getPendingTasks(): Flow<List<Task>>

    suspend fun createTask(task: Task): Long

    suspend fun updateTaskStatus(
        taskId: Long,
        completed: Boolean
    )

    suspend fun deleteTask(taskId: Long)

    suspend fun deleteAllTasks()
}
