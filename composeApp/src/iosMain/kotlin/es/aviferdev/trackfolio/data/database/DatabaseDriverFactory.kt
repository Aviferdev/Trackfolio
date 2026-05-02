package es.aviferdev.trackfolio.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(
            schema = TrackfolioDatabase.Schema,
            name = "trackfolio.db"
        )
}
