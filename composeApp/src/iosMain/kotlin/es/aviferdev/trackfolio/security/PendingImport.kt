package es.aviferdev.trackfolio.security

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSFileType
import platform.Foundation.NSFileTypeDirectory
import platform.Foundation.NSSearchPathDirectory
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

private const val DB_NAME = "trackfolio.db"
private const val PENDING_TAG = "[PendingImport]"

actual fun applyPendingDatabaseImport(): Boolean {
    val fm = NSFileManager.defaultManager

    // Mismas rutas candidatas que DatabaseBackupManager
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

    // Si no hay BD aún (instalación nueva con backup importado antes que init?),
    // buscamos pending por todo el container.
    val foundPending = findPendingRecursively(NSHomeDirectory(), maxDepth = 6)
    if (foundPending != null) {
        val targetPath = foundPending.removeSuffix(".pending")
        println("$PENDING_TAG Pending encontrado por búsqueda: $foundPending")
        return applyPending(targetPath, foundPending)
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

@OptIn(ExperimentalForeignApi::class)
private fun findPendingRecursively(rootPath: String, maxDepth: Int): String? {
    val fm = NSFileManager.defaultManager
    if (maxDepth < 0) return null
    @Suppress("UNCHECKED_CAST")
    val entries = fm.contentsOfDirectoryAtPath(rootPath, error = null) as? List<String>
        ?: return null
    for (entry in entries) {
        if (entry.endsWith(".pending") && entry.startsWith(DB_NAME)) {
            return "$rootPath/$entry"
        }
    }
    for (entry in entries) {
        val sub = "$rootPath/$entry"
        val attrs = fm.attributesOfItemAtPath(sub, error = null) ?: continue
        val type = attrs[NSFileType] as? String ?: continue
        if (type == NSFileTypeDirectory) {
            val nested = findPendingRecursively(sub, maxDepth - 1)
            if (nested != null) return nested
        }
    }
    return null
}
