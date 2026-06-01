package es.aviferdev.n3to.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = N3toDatabase.Schema,
            name = "n3to.db"
        ).also { driver ->
            driver.execute(null, "PRAGMA foreign_keys = ON", 0)
            driver.execute(null, "PRAGMA journal_mode = WAL", 0)
        }
    }
}
