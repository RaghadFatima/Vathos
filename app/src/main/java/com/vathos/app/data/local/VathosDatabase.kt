
package com.vathos.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vathos.app.data.local.dao.ChatDao
import com.vathos.app.data.local.dao.DocumentChunkDao
import com.vathos.app.data.local.dao.DocumentDao
import com.vathos.app.data.local.dao.NoteDao
import com.vathos.app.data.local.dao.TaskDao
import com.vathos.app.data.local.entity.ChatMessageEntity
import com.vathos.app.data.local.entity.DocumentChunkEntity
import com.vathos.app.data.local.entity.DocumentEntity
import com.vathos.app.data.local.entity.NoteEntity
import com.vathos.app.data.local.entity.TaskEntity

@Database(
    entities = [
        DocumentEntity::class,
        DocumentChunkEntity::class,
        ChatMessageEntity::class,
        NoteEntity::class,
        TaskEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class VathosDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao

    abstract fun documentChunkDao(): DocumentChunkDao

    abstract fun chatDao(): ChatDao

    abstract fun noteDao(): NoteDao

    abstract fun taskDao(): TaskDao
}
