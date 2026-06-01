package es.aviferdev.n3to.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = N3toDatabase.Schema,
            context = context,
            name = "n3to.db"
        ).also { driver ->
            // PRAGMAs no son soportados por execute() de SQLDelight en Android 10-
            // (usa SQLiteStatement.executeUpdateDelete internamente).
            // Se ejecutan en el callback onOpen del driver, o se silencian si fallan.
            runCatching { driver.execute(null, "PRAGMA foreign_keys = ON", 0) }
            runCatching { driver.execute(null, "PRAGMA journal_mode = WAL", 0) }
        }
    }
}
