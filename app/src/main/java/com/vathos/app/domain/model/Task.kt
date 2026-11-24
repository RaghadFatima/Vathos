
package com.vathos.app.domain.model

data class Task(
    val id: Long,
    val title: String,
    val dueDate: String,
    val isCompleted: Boolean,
    val createdAt: Long
)
