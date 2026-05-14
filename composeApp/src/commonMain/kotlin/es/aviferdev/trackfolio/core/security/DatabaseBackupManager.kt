package es.aviferdev.trackfolio.core.security

/**
 * Resultado de una operación de backup.
 */
sealed class BackupResult {
    data object Success : BackupResult()
    data class Error(val message: String) : BackupResult()
}

/**
 * Formato del fichero .trackfolio:
 *   [4 bytes]  longitud del salt (big-endian int32)
 *   [16 bytes] salt PBKDF2 aleatorio
 *   [16 bytes] IV AES-CBC aleatorio
 *   [N bytes]  datos AES-256-CBC cifrados (PKCS7)
 *
 * Clave derivada con PBKDF2-HMAC-SHA256, 100.000 iteraciones, 32 bytes.
 * La criptografía se ejecuta en commonMain (AesCrypto); solo el I/O del fichero
 * y el Share Sheet / Document Picker son expect/actual.
 */
expect class DatabaseBackupManager {
    /** Cifra la BD con [password] y lanza el Share Sheet / ACTION_SEND. */
    fun exportEncrypted(password: String, onResult: (BackupResult) -> Unit)

    /** Abre el picker de ficheros y descifra el backup seleccionado. */
    fun importEncrypted(password: String, onResult: (BackupResult) -> Unit)
}

// ─── Lógica de cifrado compartida (usada por ambos actual) ───────────────────

const val BACKUP_SALT_LEN   = 16
const val BACKUP_IV_LEN     = 16
const val BACKUP_KEY_LEN    = 32
const val BACKUP_ITERATIONS = 100_000

fun encryptBackup(dbBytes: ByteArray, password: String): ByteArray {
    val salt = kotlin.random.Random.nextBytes(BACKUP_SALT_LEN)
    val iv   = kotlin.random.Random.nextBytes(BACKUP_IV_LEN)
    val key  = AesCrypto.pbkdf2(password.encodeToByteArray(), salt, BACKUP_ITERATIONS, BACKUP_KEY_LEN)
    val enc  = AesCrypto.encryptCbc(dbBytes, key, iv)
    return AesCrypto.intToBytes(BACKUP_SALT_LEN) + salt + iv + enc
}

fun decryptBackup(fileBytes: ByteArray, password: String): ByteArray {
    val saltLen = AesCrypto.bytesToInt(fileBytes.sliceArray(0..3))
    val salt    = fileBytes.sliceArray(4 until 4 + saltLen)
    val iv      = fileBytes.sliceArray(4 + saltLen until 4 + saltLen + BACKUP_IV_LEN)
    val data    = fileBytes.sliceArray(4 + saltLen + BACKUP_IV_LEN until fileBytes.size)
    val key     = AesCrypto.pbkdf2(password.encodeToByteArray(), salt, BACKUP_ITERATIONS, BACKUP_KEY_LEN)
    return AesCrypto.decryptCbc(data, key, iv)
}
