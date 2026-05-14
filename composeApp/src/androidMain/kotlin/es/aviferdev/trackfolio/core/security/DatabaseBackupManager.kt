package es.aviferdev.trackfolio.core.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

// TODO Refactorizar

actual class DatabaseBackupManager(private val context: Context) {

    private val DB_NAME     = "trackfolio.db"
    private val BACKUP_NAME = "trackfolio_backup.trackfolio"

    private var activityRef: WeakReference<ComponentActivity>? = null

    fun bindActivity(activity: ComponentActivity) {
        activityRef = WeakReference(activity)
    }

    fun unbindActivity() {
        activityRef = null
    }

    private fun currentActivity(): ComponentActivity? = activityRef?.get()

    actual fun exportEncrypted(password: String, onResult: (BackupResult) -> Unit) {
        try {
            val activity = currentActivity() ?: run {
                onResult(BackupResult.Error("La aplicación no está en primer plano"))
                return
            }

            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                onResult(BackupResult.Error("Base de datos no encontrada"))
                return
            }

            val dbBytes   = dbFile.readBytes()
            val encrypted = encryptBackup(dbBytes, password)

            val outFile = File(context.cacheDir, BACKUP_NAME)
            FileOutputStream(outFile).use { it.write(encrypted) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                outFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Backup Trackfolio")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Exportar backup Trackfolio")
            activity.startActivity(chooser)

            onResult(BackupResult.Success)
        } catch (e: Exception) {
            onResult(BackupResult.Error(e.message ?: "Error al exportar"))
        }
    }

    actual fun importEncrypted(password: String, onResult: (BackupResult) -> Unit) {
        val activity = currentActivity() ?: run {
            onResult(BackupResult.Error("La aplicación no está en primer plano"))
            return
        }

        val launcher = activity.activityResultRegistry.register(
            "trackfolio_import_${System.currentTimeMillis()}",
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri == null) {
                onResult(BackupResult.Error("Ningún archivo seleccionado"))
                return@register
            }
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("No se puede leer el fichero")
                val fileBytes = inputStream.readBytes()
                inputStream.close()

                val decrypted = decryptBackup(fileBytes, password)

                // Validar cabecera SQLite antes de tocar nada
                if (!isValidSqliteHeader(decrypted)) {
                    onResult(BackupResult.Error("El archivo no contiene una base de datos válida"))
                    return@register
                }

                // Escribir como ".pending" en lugar de sobrescribir directamente.
                // El fichero se aplica al siguiente arranque (ver applyPendingDatabaseImport).
                // Sobrescribir mientras la BD está abierta provoca crashes y corrupción.
                val dbFile = context.getDatabasePath(DB_NAME)
                dbFile.parentFile?.mkdirs()
                val pendingFile = File(dbFile.parentFile, "$DB_NAME.pending")
                FileOutputStream(pendingFile).use { it.write(decrypted) }

                onResult(BackupResult.Success)
            } catch (e: Exception) {
                onResult(BackupResult.Error("Contraseña incorrecta o archivo dañado"))
            }
        }
        launcher.launch(arrayOf("application/octet-stream", "*/*"))
    }

    private fun isValidSqliteHeader(bytes: ByteArray): Boolean {
        val header = "SQLite format 3\u0000"
        if (bytes.size < header.length) return false
        for (i in header.indices) {
            if (bytes[i].toInt().toChar() != header[i]) return false
        }
        return true
    }
}
