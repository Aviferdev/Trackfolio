package es.aviferdev.n3to.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.Foundation.NSFileManager
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbName = "n3to.db"

        if (DESTRUCTIVE_MIGRATION_ENABLED) {
            val dbPath = findDatabasePath(dbName)
            if (dbPath != null) {
                try {
                    val existingVersion = readUserVersion(dbPath)
                    val schemaVersion = N3toDatabase.Schema.version
                    if (existingVersion != schemaVersion) {
                        deleteDatabaseFiles(dbPath)
                    }
                } catch (_: Exception) {
                    deleteDatabaseFiles(dbPath)
                }
            }
        }

        return NativeSqliteDriver(
            schema = N3toDatabase.Schema,
            name = dbName
        )
    }

    private fun findDatabasePath(dbName: String): String? {
        val fm = NSFileManager.defaultManager
        val urls = fm.URLsForDirectory(NSLibraryDirectory, NSUserDomainMask)
        val libUrl = urls.firstOrNull() as? NSURL ?: return null
        val dbUrl = libUrl.URLByAppendingPathComponent(dbName) ?: return null
        val path = dbUrl.path ?: return null
        return if (fm.fileExistsAtPath(path)) path else null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun readUserVersion(path: String): Long {
        val ptr = platform.posix.fopen(path, "rb") ?: return -1
        try {
            val header = ByteArray(64)
            val read = platform.posix.fread(header.refTo(0), 1u, 64u, ptr)
            if (read < 64u) return -1

            return ((header[60].toLong() and 0xFF) shl 24) or
                   ((header[61].toLong() and 0xFF) shl 16) or
                   ((header[62].toLong() and 0xFF) shl 8) or
                    (header[63].toLong() and 0xFF)
        } finally {
            platform.posix.fclose(ptr)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun deleteDatabaseFiles(dbPath: String) {
        val fm = NSFileManager.defaultManager
        listOf(dbPath, "$dbPath-wal", "$dbPath-shm").forEach { path ->
            if (fm.fileExistsAtPath(path)) {
                fm.removeItemAtPath(path, error = null)
            }
        }
    }
}
