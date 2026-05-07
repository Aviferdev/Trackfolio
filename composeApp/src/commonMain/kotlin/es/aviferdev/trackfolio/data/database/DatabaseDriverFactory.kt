package es.aviferdev.trackfolio.data.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Si true, la BD se borra y recrea cuando la versión del esquema cambia.
 * Usar SOLO en desarrollo. En producción debe ser false para que SQLDelight
 * ejecute las migraciones (.sqm) y conserve los datos del usuario.
 */
const val DESTRUCTIVE_MIGRATION_ENABLED = true

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
