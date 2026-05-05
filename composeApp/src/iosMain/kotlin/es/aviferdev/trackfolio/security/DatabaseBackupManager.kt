package es.aviferdev.trackfolio.security

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

private const val TAG = "[BackupManager]"

@OptIn(ExperimentalForeignApi::class)
actual class DatabaseBackupManager {

    private val DB_NAME     = "trackfolio.db"
    private val BACKUP_NAME = "trackfolio_backup.trackfolio"

    actual fun exportEncrypted(password: String, onResult: (BackupResult) -> Unit) {
        println("$TAG ▶ exportEncrypted INVOCADO (passwordLen=${password.length})")

        // Wrappear onResult para tracear cuándo se llama (y desde dónde)
        val tracedResult: (BackupResult) -> Unit = { r ->
            when (r) {
                is BackupResult.Success -> println("$TAG ✅ onResult(Success)")
                is BackupResult.Error   -> println("$TAG ❌ onResult(Error): ${r.message}")
            }
            onResult(r)
        }

        // ── Bloque 1: lectura BD + cifrado + escritura temp ──────────────────
        val outUrl: NSURL
        try {
            println("$TAG · Buscando ruta de BD…")
            val dbPath = dbFilePath()
            if (dbPath == null) {
                println("$TAG · dbFilePath() devolvió null")
                tracedResult(BackupResult.Error("Base de datos no encontrada"))
                return
            }
            println("$TAG · dbPath=$dbPath")

            val dbExists = NSFileManager.defaultManager.fileExistsAtPath(dbPath)
            println("$TAG · dbExists=$dbExists")

            val dbData = NSData.dataWithContentsOfFile(dbPath)
            if (dbData == null) {
                println("$TAG · NSData.dataWithContentsOfFile devolvió null")
                tracedResult(BackupResult.Error("No se puede leer la base de datos"))
                return
            }
            println("$TAG · BD leída: ${dbData.length} bytes")

            val dbBytes   = dbData.toKotlinByteArray()
            println("$TAG · Cifrando (${dbBytes.size} bytes)…")
            val encrypted = encryptBackup(dbBytes, password)
            println("$TAG · Cifrado OK (${encrypted.size} bytes)")

            val tempDir = NSTemporaryDirectory()
            val outPath = "$tempDir$BACKUP_NAME"
            println("$TAG · Escribiendo temp en $outPath")
            val ok = encrypted.toNSData().writeToFile(outPath, atomically = true)
            if (!ok) {
                println("$TAG · writeToFile devolvió false")
                tracedResult(BackupResult.Error("No se pudo escribir el fichero temporal"))
                return
            }
            outUrl = NSURL.fileURLWithPath(outPath)
            println("$TAG · Fichero temp listo: ${outUrl.absoluteString}")
        } catch (e: Exception) {
            println("$TAG · EXCEPCIÓN en bloque de cifrado: ${e::class.simpleName}: ${e.message}")
            tracedResult(BackupResult.Error(e.message ?: "Error al cifrar el backup"))
            return
        }

        // ── Bloque 2: presentación en main queue ─────────────────────────────
        println("$TAG · Programando dispatch_async al main queue…")
        dispatch_async(dispatch_get_main_queue()) {
            println("$TAG · [main] dispatch_async ejecutándose")

            val presenter = topViewController()
            if (presenter == null) {
                println("$TAG · [main] topViewController() devolvió null")
                tracedResult(BackupResult.Error("No se puede presentar el diálogo de compartir"))
                return@dispatch_async
            }
            println("$TAG · [main] presenter=${presenter::class.simpleName}, presentedVC=${presenter.presentedViewController?.let { it::class.simpleName } ?: "null"}")

            // Si el presenter ya está presentando otro VC, presentar encima fallaría.
            // En ese caso subimos al VC más arriba (debería estar resuelto por topViewController,
            // pero por seguridad lo verificamos).
            val realPresenter = run {
                var p: UIViewController = presenter
                while (p.presentedViewController != null) {
                    p = p.presentedViewController!!
                }
                p
            }
            println("$TAG · [main] realPresenter=${realPresenter::class.simpleName}")

            val activityVC = UIActivityViewController(
                activityItems         = listOf(outUrl),
                applicationActivities = null
            )
            println("$TAG · [main] UIActivityViewController creado")

            // En iPad
            activityVC.popoverPresentationController?.let { popover ->
                popover.sourceView = realPresenter.view
                popover.sourceRect = CGRectMakeCenter(realPresenter.view)
                println("$TAG · [main] popoverPresentationController configurado (iPad)")
            }

            println("$TAG · [main] Llamando a presentViewController…")
            realPresenter.presentViewController(activityVC, animated = true) {
                println("$TAG · [main] completion del present invocado — sheet visible")
                tracedResult(BackupResult.Success)
            }
            println("$TAG · [main] presentViewController retornó (presentación pendiente)")
        }
        println("$TAG · exportEncrypted retornando (async pendiente)")
    }

    actual fun importEncrypted(password: String, onResult: (BackupResult) -> Unit) {
        println("$TAG ▶ importEncrypted INVOCADO")

        dispatch_async(dispatch_get_main_queue()) {
            val presenter = topViewController() ?: run {
                println("$TAG · [main] topViewController() devolvió null")
                onResult(BackupResult.Error("No se puede presentar el selector"))
                return@dispatch_async
            }
            println("$TAG · [main] import presenter=${presenter::class.simpleName}")

            val realPresenter = run {
                var p: UIViewController = presenter
                while (p.presentedViewController != null) {
                    p = p.presentedViewController!!
                }
                p
            }

            val delegate = ImportPickerDelegate(
                onPick  = { fileUrl -> restoreFromUrl(fileUrl, password, onResult) },
                onError = { msg    -> onResult(BackupResult.Error(msg)) }
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
        pickerDelegateHolder = null
        try {
            val data = NSData.dataWithContentsOfURL(fileUrl) ?: run {
                onResult(BackupResult.Error("No se puede leer el archivo"))
                return
            }
            val decrypted = decryptBackup(data.toKotlinByteArray(), password)
            val dbPath = dbFilePath() ?: run {
                onResult(BackupResult.Error("No se puede localizar la BD"))
                return
            }
            decrypted.toNSData().writeToFile(dbPath, atomically = true)
            onResult(BackupResult.Success)
        } catch (e: Exception) {
            onResult(BackupResult.Error("Contraseña incorrecta o archivo dañado"))
        }
    }

    private fun dbFilePath(): String? {
        // SQLDelight NativeSqliteDriver guarda la BD por defecto en
        // Library/Caches/databases/<name>. Probamos esa ruta y otras posibles.
        val fm = NSFileManager.defaultManager

        // Candidatos por carpeta + subcarpeta:
        //   - Library/Caches/databases/<name>      ← ubicación por defecto de NativeSqliteDriver 2.x
        //   - Documents/databases/<name>           ← versiones antiguas
        //   - Documents/<name>                     ← fallback histórico
        //   - Library/Application Support/<name>   ← ubicación previa de este código
        //   - Library/<name>                       ← raro pero posible
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

        // Último recurso: búsqueda recursiva en todo el contenedor de la app
        // por un fichero llamado trackfolio.db. Solo se ejecuta si todas las
        // rutas conocidas fallan, así que es barato.
        println("$TAG · dbFilePath(): no en rutas conocidas, buscando recursivamente…")
        val homeDir = NSHomeDirectory()
        val found = findFileRecursively(homeDir, DB_NAME, maxDepth = 6)
        if (found != null) {
            println("$TAG · dbFilePath(): encontrado por búsqueda recursiva → $found")
            return found
        }

        println("$TAG · dbFilePath(): NO ENCONTRADA. Candidatos probados:")
        for ((label, path) in candidates) {
            println("$TAG     - $label: $path")
        }
        return null
    }

    /** Búsqueda recursiva sencilla por nombre exacto, limitada en profundidad. */
    private fun findFileRecursively(rootPath: String, fileName: String, maxDepth: Int): String? {
        val fm = NSFileManager.defaultManager
        if (maxDepth < 0) return null
        @Suppress("UNCHECKED_CAST")
        val entries = fm.contentsOfDirectoryAtPath(rootPath, error = null) as? List<String>
            ?: return null
        // Primero, ficheros en este nivel
        for (entry in entries) {
            if (entry == fileName) return "$rootPath/$entry"
        }
        // Luego, recursión en subdirectorios
        for (entry in entries) {
            val sub = "$rootPath/$entry"
            // Saltar enlaces simbólicos / cosas que no son carpeta
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
    println("$TAG · topViewController() inicio")
    val scenes = UIApplication.sharedApplication.connectedScenes
    println("$TAG · connectedScenes count=${scenes.size}")

    var window: UIWindow? = null

    for (scene in scenes) {
        val windowScene = scene as? UIWindowScene ?: continue
        val state = windowScene.activationState
        println("$TAG · scene activationState=$state (foregroundActive=$UISceneActivationStateForegroundActive)")
        if (state != UISceneActivationStateForegroundActive) continue

        val sceneWindows = windowScene.windows
        println("$TAG · scene.windows count=${sceneWindows.size}")
        var keyWin: UIWindow? = null
        var firstWin: UIWindow? = null
        for (w in sceneWindows) {
            val uiWin = w as? UIWindow ?: continue
            if (firstWin == null) firstWin = uiWin
            val isKey = uiWin.isKeyWindow()
            println("$TAG ·   window isKeyWindow=$isKey hidden=${uiWin.hidden}")
            if (isKey) { keyWin = uiWin; break }
        }
        window = keyWin ?: firstWin
        if (window != null) break
    }

    if (window == null) {
        println("$TAG · sin foregroundActive — fallback a primera scene")
        outer@ for (scene in scenes) {
            val ws = scene as? UIWindowScene ?: continue
            for (w in ws.windows) {
                val uiWin = w as? UIWindow ?: continue
                window = uiWin
                break@outer
            }
        }
    }

    if (window == null) {
        println("$TAG · No se encontró ninguna window")
        return null
    }
    println("$TAG · window encontrada, rootVC=${window?.rootViewController?.let { it::class.simpleName } ?: "null"}")

    var top: UIViewController? = window?.rootViewController
    var depth = 0
    while (top?.presentedViewController != null && depth < 10) {
        println("$TAG ·   subiendo: ${top!!::class.simpleName} → ${top!!.presentedViewController!!::class.simpleName}")
        top = top!!.presentedViewController
        depth++
    }
    println("$TAG · topViewController() devuelve ${top?.let { it::class.simpleName } ?: "null"}")
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
