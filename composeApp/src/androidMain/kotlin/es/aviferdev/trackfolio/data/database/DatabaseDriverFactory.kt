package es.aviferdev.trackfolio.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        val dbName = "trackfolio.db"

        if (DESTRUCTIVE_MIGRATION_ENABLED) {
            val dbFile = context.getDatabasePath(dbName)
            if (dbFile.exists()) {
                try {
                    val existingVersion = readUserVersion(dbFile.absolutePath)
                    val schemaVersion = TrackfolioDatabase.Schema.version
                    if (existingVersion != schemaVersion) {
                        context.deleteDatabase(dbName)
                        val parent = dbFile.parentFile
                        if (parent != null) {
                            listOf("$dbName-wal", "$dbName-shm", "$dbName-journal").forEach { name ->
                                java.io.File(parent, name).delete()
                            }
                        }
                    }
                } catch (_: Exception) {
                    context.deleteDatabase(dbName)
                    val parent = dbFile.parentFile
                    if (parent != null) {
                        listOf("$dbName-wal", "$dbName-shm", "$dbName-journal").forEach { name ->
                            java.io.File(parent, name).delete()
                        }
                    }
                }
            }
        }

        return AndroidSqliteDriver(
            schema = TrackfolioDatabase.Schema,
            context = context,
            name = dbName
        )
    }

    private fun readUserVersion(path: String): Long {
        val db = android.database.sqlite.SQLiteDatabase.openDatabase(
            path, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY
        )
        return try {
            db.rawQuery("PRAGMA user_version", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else 0L
            }
        } finally {
            db.close()
        }
    }
}
