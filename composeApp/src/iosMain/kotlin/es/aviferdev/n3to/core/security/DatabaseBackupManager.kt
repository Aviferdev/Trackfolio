package es.aviferdev.n3to.core.security

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

//TODO Refactorizar y modular clase completa.

private const val TAG = "[BackupManager]"

@OptIn(ExperimentalForeignApi::class)
actual class DatabaseBackupManager {

    private val DB_NAME     = "n3to.db"
    private val BACKUP_NAME = "n3to_backup.n3to"

    actual fun exportEncrypted(password: String, onResult: (BackupResult) -> Unit) {
        println("$TAG ▶ exportEncrypted INVOCADO (passwordLen=${password.length})")

        val tracedResult: (BackupResult) -> Unit = { r ->
            when (r) {
                is BackupResult.Success -> println("$TAG ✅ onResult(Success)")
                is BackupResult.Error   -> println("$TAG ❌ onResult(Error): ${r.message}")
            }
            onResult(r)
        }

        val outUrl: NSURL
        try {
            val dbPath = dbFilePath()
            if (dbPath == null) {
                tracedResult(BackupResult.Error("Base de datos no encontrada"))
                return
            }

            val dbData = NSData.dataWithContentsOfFile(dbPath)
            if (dbData == null) {
                tracedResult(BackupResult.Error("No se puede leer la base de datos"))
                return
            }

            val dbBytes   = dbData.toKotlinByteArray()
            val encrypted = encryptBackup(dbBytes, password)

            val tempDir = NSTemporaryDirectory()
            val outPath = "$tempDir$BACKUP_NAME"
            val ok = encrypted.toNSData().writeToFile(outPath, atomically = true)
            if (!ok) {
                tracedResult(BackupResult.Error("No se pudo escribir el fichero temporal"))
                return
            }
            outUrl = NSURL.fileURLWithPath(outPath)
        } catch (e: Exception) {
            tracedResult(BackupResult.Error(e.message ?: "Error al cifrar el backup"))
            return
        }

        dispatch_async(dispatch_get_main_queue()) {
            val presenter = topViewController()
            if (presenter == null) {
                tracedResult(BackupResult.Error("No se puede presentar el diálogo de compartir"))
                return@dispatch_async
            }

            val realPresenter = run {
                var p: UIViewController = presenter
                while (p.presentedViewController != null) {
                    p = p.presentedViewController!!
                }
                p
            }

            val activityVC = UIActivityViewController(
                activityItems         = listOf(outUrl),
                applicationActivities = null
            )
            activityVC.popoverPresentationController?.let { popover ->
                popover.sourceView = realPresenter.view
                popover.sourceRect = CGRectMakeCenter(realPresenter.view)
            }
            realPresenter.presentViewController(activityVC, animated = true) {
                tracedResult(BackupResult.Success)
            }
        }
    }

    actual fun importEncrypted(password: String, onResult: (BackupResult) -> Unit) {
        println("$TAG ▶ importEncrypted INVOCADO")

        val tracedResult: (BackupResult) -> Unit = { r ->
            when (r) {
                is BackupResult.Success -> println("$TAG ✅ import onResult(Success)")
                is BackupResult.Error   -> println("$TAG ❌ import onResult(Error): ${r.message}")
            }
            onResult(r)
        }

        dispatch_async(dispatch_get_main_queue()) {
            val presenter = topViewController() ?: run {
                tracedResult(BackupResult.Error("No se puede presentar el selector"))
                return@dispatch_async
            }

            val realPresenter = run {
                var p: UIViewController = presenter
                while (p.presentedViewController != null) {
                    p = p.presentedViewController!!
                }
                p
            }

            // Creamos el delegate y lo retenemos en variable de instancia para
            // que no sea recolectado mientras el picker está visible.
            val delegate = ImportPickerDelegate(
                onPick  = { fileUrl ->
                    println("$TAG · documentPicker.onPick recibido")
                    // Diferimos el restore para dejar que el picker termine su animación
                    // de dismiss antes de procesar (evita crashes de UIKit por
                    // operaciones costosas dentro del callback).
                    dispatch_async(dispatch_get_main_queue()) {
                        restoreFromUrl(fileUrl, password, tracedResult)
                    }
                },
                onError = { msg ->
                    println("$TAG · documentPicker.onError: $msg")
                    pickerDelegateHolder = null
                    tracedResult(BackupResult.Error(msg))
                }
            )

            val picker = UIDocumentPickerViewController(
                forOpeningContentTypes = listOf("public.data")
            )
            picker.delegate = delegate
            picker.allowsMultipleSelection = false

            pickerDelegateHolder = delegate

            realPresenter.presentViewController(picker, animated = true, completion = null)
        }
    }

    private fun restoreFromUrl(fileUrl: NSURL, password: String, onResult: (BackupResult) -> Unit) {
        println("$TAG · restoreFromUrl iniciado: ${fileUrl.absoluteString}")

        // Liberamos el delegate ahora que el picker ya cerró.
        pickerDelegateHolder = null

        // ── 1) Acceso security-scoped al fichero seleccionado ───────────────
        // Los UIDocumentPicker entregan URLs con security scope: hay que pedir
        // permiso explícito antes de leer y liberar después, o el read falla.
        val accessGranted = fileUrl.startAccessingSecurityScopedResource()
        println("$TAG · startAccessingSecurityScopedResource=$accessGranted")

        try {
            // ── 2) Coordinar la lectura con NSFileCoordinator ───────────────
            // El proveedor del fichero (Files, iCloud, etc.) puede tenerlo
            // bloqueado o necesitar descargarlo. NSFileCoordinator gestiona eso.
            val data = readDataCoordinated(fileUrl)
            if (data == null) {
                onResult(BackupResult.Error("No se puede leer el archivo seleccionado"))
                return
            }
            println("$TAG · Archivo leído: ${data.length} bytes")

            // ── 3) Descifrar ────────────────────────────────────────────────
            val decrypted: ByteArray = try {
                decryptBackup(data.toKotlinByteArray(), password)
            } catch (e: Exception) {
                println("$TAG · Fallo al descifrar: ${e::class.simpleName}: ${e.message}")
                onResult(BackupResult.Error("Contraseña incorrecta o archivo dañado"))
                return
            }
            println("$TAG · Descifrado OK: ${decrypted.size} bytes")

            // ── 4) Validar cabecera SQLite antes de sobrescribir ────────────
            // Una BD válida empieza con los bytes 'SQLite format 3\u0000'.
            // Si descifró pero el contenido no es una BD, no pisamos nada.
            if (!isValidSqliteHeader(decrypted)) {
                println("$TAG · Cabecera SQLite no válida tras descifrar")
                onResult(BackupResult.Error("El archivo no contiene una base de datos válida"))
                return
            }

            // ── 5) Localizar la BD actual ───────────────────────────────────
            val dbPath = dbFilePath() ?: run {
                onResult(BackupResult.Error("No se puede localizar la BD actual"))
                return
            }
            println("$TAG · BD actual: $dbPath")

            // ── 6) Escribir a un fichero temporal "pending" ─────────────────
            // No sobrescribimos directamente la BD porque está abierta por
            // SQLDelight: hacerlo provoca crashes (handles inválidos, cache
            // page mismatch). En su lugar, dejamos un fichero pendiente que
            // se aplica al próximo arranque de la app.
            val pendingPath = "$dbPath.pending"
            val pendingNSData = decrypted.toNSData()
            val writeOk = pendingNSData.writeToFile(pendingPath, atomically = true)
            if (!writeOk) {
                println("$TAG · No se pudo escribir el fichero pending")
                onResult(BackupResult.Error("No se pudo preparar la importación"))
                return
            }
            println("$TAG · Fichero pending escrito: $pendingPath")

            // ── 7) Devolver Success con un mensaje específico ───────────────
            // El llamador es responsable de pedir al usuario que reinicie la
            // app. La aplicación del backup ocurre en el próximo arranque.
            onResult(BackupResult.Success)

        } finally {
            if (accessGranted) {
                fileUrl.stopAccessingSecurityScopedResource()
            }
        }
    }

    /**
     * Lee el contenido del fichero usando NSFileCoordinator. Esto fuerza la
     * descarga si está en iCloud y respeta los locks del proveedor.
     */
    private fun readDataCoordinated(url: NSURL): NSData? {
        val coordinator = NSFileCoordinator(filePresenter = null)
        var result: NSData? = null
        var coordError: NSError? = null

        coordinator.coordinateReadingItemAtURL(
            url,
            options = NSFileCoordinatorReadingWithoutChanges,
            error = null
        ) { coordinatedUrl ->
            if (coordinatedUrl != null) {
                result = NSData.dataWithContentsOfURL(coordinatedUrl)
            }
        }

        if (coordError != null) {
            println("$TAG · NSFileCoordinator error: ${coordError?.localizedDescription}")
        }
        return result
    }

    /** Comprueba el "magic header" de SQLite. */
    private fun isValidSqliteHeader(bytes: ByteArray): Boolean {
        // "SQLite format 3\u0000" = 16 bytes
        val header = "SQLite format 3\u0000"
        if (bytes.size < header.length) return false
        for (i in header.indices) {
            if (bytes[i].toInt().toChar() != header[i]) return false
        }
        return true
    }

    private fun dbFilePath(): String? {
        val fm = NSFileManager.defaultManager

        val candidates = mutableListOf<Pair<String, String?>>()

        fun addCandidate(label: String, dir: NSSearchPathDirectory, subFolder: String? = null) {
            val urls = fm.URLsForDirectory(dir, NSUserDomainMask)
            val base = urls.firstOrNull() as? NSURL ?: return
            val withSub = if (subFolder != null)
                base.URLByAppendingPathComponent(subFolder)?.URLByAppendingPathComponent(DB_NAME)
            else
                base.URLByAppendingPathComponent(DB_NAME)
            candidates.add(label to withSub?.path)
        }

        addCandidate("Caches/databases",            NSCachesDirectory,            "databases")
        addCandidate("Documents/databases",         NSDocumentDirectory,          "databases")
        addCandidate("Documents",                   NSDocumentDirectory)
        addCandidate("ApplicationSupport/databases",NSApplicationSupportDirectory,"databases")
        addCandidate("ApplicationSupport",          NSApplicationSupportDirectory)
        addCandidate("Library",                     NSLibraryDirectory)
        addCandidate("Caches",                      NSCachesDirectory)

        for ((label, path) in candidates) {
            if (path != null && fm.fileExistsAtPath(path)) {
                println("$TAG · dbFilePath(): encontrado en $label → $path")
                return path
            }
        }

        // Búsqueda recursiva como último recurso
        val homeDir = NSHomeDirectory()
        val found = findFileRecursively(homeDir, DB_NAME, maxDepth = 6)
        if (found != null) {
            println("$TAG · dbFilePath(): encontrado por búsqueda recursiva → $found")
            return found
        }

        println("$TAG · dbFilePath(): NO ENCONTRADA")
        return null
    }

    private fun findFileRecursively(rootPath: String, fileName: String, maxDepth: Int): String? {
        val fm = NSFileManager.defaultManager
        if (maxDepth < 0) return null
        @Suppress("UNCHECKED_CAST")
        val entries = fm.contentsOfDirectoryAtPath(rootPath, error = null) as? List<String>
            ?: return null
        for (entry in entries) {
            if (entry == fileName) return "$rootPath/$entry"
        }
        for (entry in entries) {
            val sub = "$rootPath/$entry"
            val attrs = fm.attributesOfItemAtPath(sub, error = null) ?: continue
            val type = attrs[NSFileType] as? String ?: continue
            if (type == NSFileTypeDirectory) {
                val nested = findFileRecursively(sub, fileName, maxDepth - 1)
                if (nested != null) return nested
            }
        }
        return null
    }

    private fun ByteArray.toNSData(): NSData =
        this.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), this.size.toULong())
        }

    private fun NSData.toKotlinByteArray(): ByteArray {
        val len    = this.length.toInt()
        val result = ByteArray(len)
        if (len > 0) {
            val ptr = this.bytes ?: return result
            result.usePinned { pinned ->
                memcpy(pinned.addressOf(0), ptr, this.length)
            }
        }
        return result
    }

    private var pickerDelegateHolder: ImportPickerDelegate? = null
}

// ─── Helpers de UIKit ─────────────────────────────────────────────────────────

@OptIn(ExperimentalForeignApi::class)
private fun topViewController(): UIViewController? {
    val scenes = UIApplication.sharedApplication.connectedScenes

    var window: UIWindow? = null
    for (scene in scenes) {
        val windowScene = scene as? UIWindowScene ?: continue
        if (windowScene.activationState != UISceneActivationStateForegroundActive) continue

        val sceneWindows = windowScene.windows
        var keyWin: UIWindow? = null
        var firstWin: UIWindow? = null
        for (w in sceneWindows) {
            val uiWin = w as? UIWindow ?: continue
            if (firstWin == null) firstWin = uiWin
            if (uiWin.isKeyWindow()) { keyWin = uiWin; break }
        }
        window = keyWin ?: firstWin
        if (window != null) break
    }

    if (window == null) {
        outer@ for (scene in scenes) {
            val ws = scene as? UIWindowScene ?: continue
            for (w in ws.windows) {
                val uiWin = w as? UIWindow ?: continue
                window = uiWin
                break@outer
            }
        }
    }

    if (window == null) return null

    var top: UIViewController? = window?.rootViewController
    var depth = 0
    while (top?.presentedViewController != null && depth < 10) {
        top = top!!.presentedViewController
        depth++
    }
    return top
}

@OptIn(ExperimentalForeignApi::class)
private fun CGRectMakeCenter(view: UIView): CValue<CGRect> {
    return view.bounds.useContents {
        CGRectMake(
            x = origin.x + size.width  / 2,
            y = origin.y + size.height / 2,
            width  = 0.0,
            height = 0.0
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
class ImportPickerDelegate(
    private val onPick:  (NSURL) -> Unit,
    private val onError: (String) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url != null) onPick(url)
        else onError("Ningún archivo seleccionado")
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onError("Importación cancelada")
    }

    override fun isEqual(other: Any?): Boolean = this === other
    override fun hash(): ULong = hashCode().toULong()
}
