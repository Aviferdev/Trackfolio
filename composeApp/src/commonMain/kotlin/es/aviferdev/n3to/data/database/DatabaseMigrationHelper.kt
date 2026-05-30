package es.aviferdev.n3to.data.database

import es.aviferdev.n3to.core.security.AppSettings

/**
 * Utilidades para gestionar las migraciones del esquema de base de datos.
 *
 * SQLDelight maneja las migraciones automáticamente cuando el esquema evoluciona:
 * - Los archivos `.sq` reflejan la versión más reciente del esquema.
 * - Los archivos `.sqm` contienen las sentencias DDL para migrar entre versiones.
 * - El objeto [N3toDatabase.Schema] aplica las migraciones en orden al abrir la BD.
 *
 * Este helper centraliza las comprobaciones de versión y proporciona hooks
 * para realizar copias de seguridad antes de migraciones destructivas.
 *
 * ## Flujo de migración completa
 *
 * 1. Modificar archivos `.sq` (añadir/eliminar/modificar tablas).
 * 2. Crear `composeApp/src/commonMain/sqldelight/es/aviferdev/n3to/data/database/<N>.sqm`
 *    siendo `<N>` la versión desde la que se migra (1, 2, 3…).
 * 3. Incrementar la propiedad `version` en `composeApp/build.gradle.kts`.
 * 4. SQLDelight genera automáticamente el nuevo `Schema` con las migraciones.
 *
 * @see <a href="https://cashapp.github.io/sqldelight/2.0.0/migrations/">SQLDelight Migrations</a>
 */
class DatabaseMigrationHelper(
    private val appSettings: AppSettings,
    private val database: N3toDatabase,
) {
    companion object {
        /**
         * Versión actual del esquema. Debe coincidir con el valor de
         * `version` en `composeApp/build.gradle.kts`.
         *
         * Al incrementar este valor, crear el archivo `.sqm` correspondiente
         * ANTES de lanzar la nueva versión de la app.
         */
        const val CURRENT_SCHEMA_VERSION = 1

        /**
         * Clave en [AppSettings] para persistir la versión con la que
         * se abrió correctamente la base de datos por última vez.
         */
        private const val KEY_LAST_KNOWN_VERSION = "db_schema_version"
    }

    /**
     * Versión del esquema registrada en la última ejecución exitosa.
     * - Si es menor que [CURRENT_SCHEMA_VERSION], se detecta una migración pendiente.
     * - Si es mayor, la app se abrió con una versión anterior (downgrade no soportado).
     */
    fun lastKnownVersion(): Int = appSettings.getInt(KEY_LAST_KNOWN_VERSION, 0)

    /**
     * Indica si la base de datos necesita una migración (esquema local
     * más reciente que la versión registrada).
     */
    fun isMigrationPending(): Boolean = lastKnownVersion() < CURRENT_SCHEMA_VERSION

    /**
     * Indica si se ha detectado un downgrade (la app actual es anterior
     * a la que abrió la BD por última vez).
     */
    fun isDowngradeDetected(): Boolean = lastKnownVersion() > CURRENT_SCHEMA_VERSION

    /**
     * Registra la versión actual como "ejecutada correctamente".
     * Debe llamarse DESPUÉS de que SQLDelight haya aplicado las migraciones
     * (es decir, después de la primera query exitosa sobre la BD).
     */
    fun markMigrationComplete() {
        appSettings.putInt(KEY_LAST_KNOWN_VERSION, CURRENT_SCHEMA_VERSION)
    }

    /**
     * Comprueba que la base de datos es accesible ejecutando una query trivial.
     * Usa una query generada real para verificar la conectividad SQLDelight.
     * Lanza excepción si algo va mal.
     */
    fun verifyConnectivity() {
        database.assetCategoryQueries.countAll().executeAsOne()
    }

    /**
     * Versión legible del estado de migración para logging/depuración.
     */
    fun migrationStatusString(): String {
        val lastVersion = lastKnownVersion()
        val current = CURRENT_SCHEMA_VERSION
        return when {
            lastVersion == 0       -> "BD nueva (sin versión previa)"
            lastVersion == current -> "BD actualizada (v$current)"
            lastVersion < current  -> "Migración pendiente: v$lastVersion → v$current"
            else                   -> "DOWNGRADE detectado: BD espera v$lastVersion, app tiene v$current"
        }
    }
}
