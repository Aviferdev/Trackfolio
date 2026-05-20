package es.aviferdev.n3to.core.security

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathDirectory
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

private const val DB_NAME = "n3to.db"
private const val PENDING_TAG = "[PendingImport]"

@OptIn(ExperimentalForeignApi::class)
actual fun applyPendingDatabaseImport(): Boolean {
    val fm = NSFileManager.defaultManager

    val candidates = mutableListOf<String>()

    fun addCandidate(dir: NSSearchPathDirectory, subFolder: String? = null) {
        val urls = fm.URLsForDirectory(dir, NSUserDomainMask)
        val base = urls.firstOrNull() as? NSURL ?: return
        val withSub = if (subFolder != null)
            base.URLByAppendingPathComponent(subFolder)?.URLByAppendingPathComponent(DB_NAME)
        else
            base.URLByAppendingPathComponent(DB_NAME)
        withSub?.path?.let { candidates.add(it) }
    }

    addCandidate(NSCachesDirectory,            "databases")
    addCandidate(NSDocumentDirectory,          "databases")
    addCandidate(NSDocumentDirectory)
    addCandidate(NSApplicationSupportDirectory,"databases")
    addCandidate(NSApplicationSupportDirectory)
    addCandidate(NSLibraryDirectory)
    addCandidate(NSCachesDirectory)

    // Buscar un .pending junto a alguna BD existente
    for (dbPath in candidates) {
        val pendingPath = "$dbPath.pending"
        if (fm.fileExistsAtPath(pendingPath)) {
            println("$PENDING_TAG Pending encontrado: $pendingPath")
            return applyPending(dbPath, pendingPath)
        }
    }

    return false
}

@OptIn(ExperimentalForeignApi::class)
private fun applyPending(dbPath: String, pendingPath: String): Boolean {
    val fm = NSFileManager.defaultManager

    // Asegurar que la carpeta destino existe
    val parentDir = (dbPath as String).substringBeforeLast('/', "")
    if (parentDir.isNotEmpty() && !fm.fileExistsAtPath(parentDir)) {
        fm.createDirectoryAtPath(parentDir, withIntermediateDirectories = true, attributes = null, error = null)
    }

    // Borrar la BD existente y los ficheros auxiliares de SQLite (-wal, -shm).
    // Si los dejamos, pueden corromper la BD recién importada.
    listOf(dbPath, "$dbPath-wal", "$dbPath-shm").forEach { p ->
        if (fm.fileExistsAtPath(p)) {
            val ok = fm.removeItemAtPath(p, error = null)
            println("$PENDING_TAG Eliminando $p → $ok")
        }
    }

    // Mover el pending a la ruta final
    val moveOk = fm.moveItemAtPath(pendingPath, toPath = dbPath, error = null)
    if (!moveOk) {
        // Fallback: copiar y borrar
        val copyOk = fm.copyItemAtPath(pendingPath, toPath = dbPath, error = null)
        if (copyOk) {
            fm.removeItemAtPath(pendingPath, error = null)
        } else {
            println("$PENDING_TAG ❌ No se pudo aplicar pending (move y copy fallaron)")
            return false
        }
    }

    println("$PENDING_TAG ✅ Pending aplicado correctamente sobre $dbPath")
    return true
}

