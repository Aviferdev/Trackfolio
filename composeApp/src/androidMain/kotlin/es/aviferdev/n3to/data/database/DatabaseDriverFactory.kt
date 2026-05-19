package es.aviferdev.n3to.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import java.io.RandomAccessFile

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        val dbName = "n3to.db"

        if (DESTRUCTIVE_MIGRATION_ENABLED) {
            val dbFile = context.getDatabasePath(dbName)
            if (dbFile.exists()) {
                try {
                    val existingVersion = readUserVersion(dbFile)
                    val schemaVersion = N3toDatabase.Schema.version
                    if (existingVersion != schemaVersion) {
                        deleteDatabaseSafely(context, dbName, dbFile)
                    }
                } catch (_: Exception) {
                    deleteDatabaseSafely(context, dbName, dbFile)
                }
            }
        }

        return AndroidSqliteDriver(
            schema = N3toDatabase.Schema,
            context = context,
            name = dbName
        )
    }

    /**
     * Lee la versión del esquema almacenada en los bytes 60-63 de la cabecera del archivo SQLite.
     * Este valor se corresponde con [N3toDatabase.Schema.version].
     */
    private fun readUserVersion(dbFile: java.io.File): Long {
        RandomAccessFile(dbFile, "r").use { raf ->
            raf.seek(60)
            val buffer = ByteArray(4)
            raf.readFully(buffer)
            return ((buffer[0].toLong() and 0xFF) shl 24) or
                    ((buffer[1].toLong() and 0xFF) shl 16) or
                    ((buffer[2].toLong() and 0xFF) shl 8) or
                    (buffer[3].toLong() and 0xFF)
        }
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
