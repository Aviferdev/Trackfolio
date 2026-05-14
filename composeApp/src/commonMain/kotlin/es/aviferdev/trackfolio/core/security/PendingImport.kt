package es.aviferdev.trackfolio.core.security

/**
 * Aplica un fichero `<dbName>.pending` (importación de backup) si existe,
 * sobrescribiendo la BD actual. Debe llamarse ANTES de inicializar el
 * driver de SQLite, ya que sustituye el fichero a nivel de sistema de
 * archivos. Si no hay pending, no hace nada.
 *
 * Devuelve true si se aplicó un import pendiente.
 */
expect fun applyPendingDatabaseImport(): Boolean
