package es.aviferdev.n3to.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        val dbName = "n3to.db"

        if (DESTRUCTIVE_MIGRATION_ENABLED) {
            val dbFile = context.getDatabasePath(dbName)
            if (dbFile.exists()) {
                deleteDatabaseSafely(context, dbName, dbFile)
            }
        }

        return AndroidSqliteDriver(
            schema = N3toDatabase.Schema,
            context = context,
            name = dbName
        )
    }

    private fun deleteDatabaseSafely(context: Context, dbName: String, dbFile: java.io.File) {
        context.deleteDatabase(dbName)
        val parent = dbFile.parentFile
        if (parent != null) {
            listOf("$dbName-wal", "$dbName-shm", "$dbName-journal").forEach { name ->
                java.io.File(parent, name).delete()
            }
        }
    }
}
