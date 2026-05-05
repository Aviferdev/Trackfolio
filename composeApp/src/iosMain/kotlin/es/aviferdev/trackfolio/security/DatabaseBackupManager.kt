package es.aviferdev.trackfolio.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
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
            encrypted.toNSData().writeToFile(outPath, atomically = true)
            val outUrl = NSURL.fileURLWithPath(outPath)

            dispatch_async(dispatch_get_main_queue()) {
                val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
                    ?: return@dispatch_async
                val activityVC = UIActivityViewController(
                    activityItems         = listOf(outUrl),
                    applicationActivities = null
                )
                rootVC.presentViewController(activityVC, animated = true, completion = null)
                onResult(BackupResult.Success)
            }
        } catch (e: Exception) {
            onResult(BackupResult.Error(e.message ?: "Error al exportar"))
        }
    }

    actual fun importEncrypted(password: String, onResult: (BackupResult) -> Unit) {
        dispatch_async(dispatch_get_main_queue()) {
            val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
                ?: run { onResult(BackupResult.Error("No se puede presentar el selector")); return@dispatch_async }

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

            // Retenemos el delegado vía associated object para que no sea recolectado
            // mientras el picker está visible
            pickerDelegateHolder = delegate

            rootVC.presentViewController(picker, animated = true, completion = null)
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
