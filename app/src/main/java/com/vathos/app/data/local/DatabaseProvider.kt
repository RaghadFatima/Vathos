
package com.vathos.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE documents " +
                        "ADD COLUMN extractedText TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS document_chunks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    documentId INTEGER NOT NULL,
                    chunkIndex INTEGER NOT NULL,
                    content TEXT NOT NULL,
                    FOREIGN KEY(documentId)
                        REFERENCES documents(id)
                        ON UPDATE NO ACTION
                        ON DELETE CASCADE
                )
                """.trimIndent()
            )

            database.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS
                index_document_chunks_documentId_chunkIndex
                ON document_chunks(documentId, chunkIndex)
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                ALTER TABLE document_chunks
                ADD COLUMN embedding TEXT NOT NULL DEFAULT ''
                """.trimIndent()
            )
        }
    }

    @Volatile
    private var INSTANCE: VathosDatabase? = null

    fun getDatabase(context: Context): VathosDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                VathosDatabase::class.java,
                "vathos_database"
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4
                )
                .build()
                .also {
                    INSTANCE = it
                }
        }
    }
}
