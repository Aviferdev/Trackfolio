package es.aviferdev.trackfolio.security

import android.content.Context

// TODO Refactorizar

private const val DB_NAME = "trackfolio.db"
private const val PENDING_TAG = "[PendingImport]"

private var appContextHolder: Context? = null

fun setAppContextForPendingImport(ctx: Context) {
    appContextHolder = ctx.applicationContext
}

actual fun applyPendingDatabaseImport(): Boolean {
    val ctx = appContextHolder ?: run {
        println("$PENDING_TAG context no inicializado, salto check")
        return false
    }

    val dbFile = ctx.getDatabasePath(DB_NAME)
    val pendingFile = java.io.File(dbFile.parentFile, "$DB_NAME.pending")
    if (!pendingFile.exists()) return false

    println("$PENDING_TAG Pending detectado: ${pendingFile.absolutePath}")

    // Asegurar carpeta destino
    dbFile.parentFile?.mkdirs()

    // Borrar la BD anterior y sus auxiliares (-journal, -wal, -shm) si existen
    val parent = dbFile.parentFile ?: return false
    listOf(DB_NAME, "$DB_NAME-journal", "$DB_NAME-wal", "$DB_NAME-shm").forEach { name ->
        val f = java.io.File(parent, name)
        if (f.exists()) {
            val ok = f.delete()
            println("$PENDING_TAG Borrado $name → $ok")
        }
    }

    // Renombrar pending → BD definitiva
    val renamed = pendingFile.renameTo(dbFile)
    if (!renamed) {
        // Fallback: copia y borra
        try {
            pendingFile.copyTo(dbFile, overwrite = true)
            pendingFile.delete()
        } catch (e: Exception) {
            println("$PENDING_TAG ❌ No se pudo aplicar pending: ${e.message}")
            return false
        }
    }

    println("$PENDING_TAG ✅ Pending aplicado correctamente")
    return true
}
