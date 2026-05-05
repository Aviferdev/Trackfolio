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

@OptIn(ExperimentalForeignApi::class)
actual class DatabaseBackupManager {

    private val DB_NAME     = "trackfolio.db"
    private val BACKUP_NAME = "trackfolio_backup.trackfolio"

    actual fun exportEncrypted(password: String, onResult: (BackupResult) -> Unit) {
        // Preparamos el fichero cifrado fuera del hilo principal-async para
        // que cualquier excepción suba al catch y se reporte al usuario.
        val outUrl: NSURL
        try {
            val dbPath = dbFilePath() ?: run {
                onResult(BackupResult.Error("Base de datos no encontrada"))
                return
            }
            val dbData = NSData.dataWithContentsOfFile(dbPath) ?: run {
                onResult(BackupResult.Error("No se puede leer la base de datos"))
                return
            }

            val dbBytes   = dbData.toKotlinByteArray()
            val encrypted = encryptBackup(dbBytes, password)

            val tempDir = NSTemporaryDirectory()
            val outPath = "$tempDir$BACKUP_NAME"
            val ok = encrypted.toNSData().writeToFile(outPath, atomically = true)
            if (!ok) {
                onResult(BackupResult.Error("No se pudo escribir el fichero temporal"))
                return
            }
            outUrl = NSURL.fileURLWithPath(outPath)
        } catch (e: Exception) {
            onResult(BackupResult.Error(e.message ?: "Error al cifrar el backup"))
            return
        }

        // Presentación del UIActivityViewController en main queue
        dispatch_async(dispatch_get_main_queue()) {
            val presenter = topViewController()
            if (presenter == null) {
                onResult(BackupResult.Error("No se puede presentar el diálogo de compartir"))
                return@dispatch_async
            }

            val activityVC = UIActivityViewController(
                activityItems         = listOf(outUrl),
                applicationActivities = null
            )

            // En iPad, UIActivityViewController DEBE tener configurado un
            // popoverPresentationController.sourceView/sourceRect o lanza una
            // excepción silenciosa y no se muestra nada. En iPhone, esta propiedad
            // es null y el ?.let no ejecuta el bloque.
            activityVC.popoverPresentationController?.let { popover ->
                popover.sourceView = presenter.view
                popover.sourceRect = CGRectMakeCenter(presenter.view)
            }

            presenter.presentViewController(activityVC, animated = true) {
                // El completion del present se invoca cuando el sheet ya está visible
                onResult(BackupResult.Success)
            }
        }
    }

    actual fun importEncrypted(password: String, onResult: (BackupResult) -> Unit) {
        dispatch_async(dispatch_get_main_queue()) {
            val presenter = topViewController() ?: run {
                onResult(BackupResult.Error("No se puede presentar el selector"))
                return@dispatch_async
            }

            // Instancia nueva cada vez — evita el bug del codegen con object+NSObject en K/N 2.1.0
            val delegate = ImportPickerDelegate(
                onPick  = { fileUrl -> restoreFromUrl(fileUrl, password, onResult) },
                onError = { msg    -> onResult(BackupResult.Error(msg)) }
            )

            val picker = UIDocumentPickerViewController(
                forOpeningContentTypes = listOf("public.data")
            )
            picker.delegate = delegate
            picker.allowsMultipleSelection = false

            // Retenemos el delegado para que no sea recolectado mientras el picker está visible
            pickerDelegateHolder = delegate

            presenter.presentViewController(picker, animated = true, completion = null)
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
        val urls = NSFileManager.defaultManager.URLsForDirectory(
            NSApplicationSupportDirectory, NSUserDomainMask
        )
        val base = urls.firstOrNull() as? NSURL ?: return null
        return base.URLByAppendingPathComponent(DB_NAME)?.path
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

    // Retiene el delegado mientras el picker está visible (evita GC prematuro)
    private var pickerDelegateHolder: ImportPickerDelegate? = null
}

// ─── Helpers de UIKit ─────────────────────────────────────────────────────────

/**
 * Obtiene el ViewController más arriba en la jerarquía de presentación.
 * Maneja correctamente iOS 13+ con UIScene (donde keyWindow puede ser null
 * o estar mal definido) iterando las connectedScenes activas.
 */
@OptIn(ExperimentalForeignApi::class)
private fun topViewController(): UIViewController? {
    // 1) Buscar la window activa entre las escenas conectadas (iOS 13+).
    val scenes = UIApplication.sharedApplication.connectedScenes
    var window: UIWindow? = null

    for (scene in scenes) {
        val windowScene = scene as? UIWindowScene ?: continue
        if (windowScene.activationState != UISceneActivationStateForegroundActive) continue

        // windows es NSArray; iteramos buscando la key window y, en su defecto, la primera.
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

    // 2) Fallback: primera window de cualquier escena conectada
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

    // 3) Subir hasta el VC presentado más arriba
    var top: UIViewController? = window?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    return top
}

/** Centro del view, para anclar popovers en iPad. */
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

// ─── Delegado UIDocumentPickerViewController ──────────────────────────────────
// Clase normal (no object) para evitar el bug de K/N 2.1.0 con object+NSObject
// en inicializadores de campos globales.
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
