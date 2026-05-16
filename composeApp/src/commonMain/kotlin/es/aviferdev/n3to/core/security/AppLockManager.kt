package es.aviferdev.n3to.core.security

private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

/**
 * Gestiona el bloqueo de la app.
 * La preferencia de biometría se persiste en AppSettings (SharedPrefs / NSUserDefaults).
 * El estado de bloqueo (_isLocked) es solo en memoria — se resetea al matar la app
 * pero se activa correctamente al volver de background si estaba habilitado.
 */
class AppLockManager(private val settings: AppSettings) {

    private var _isLocked = false
    val isLocked: Boolean get() = _isLocked

    /** Lee la preferencia persistida */
    val biometricEnabled: Boolean
        get() = settings.getBool(KEY_BIOMETRIC_ENABLED, false)

    fun enableBiometric() {
        settings.putBool(KEY_BIOMETRIC_ENABLED, true)
    }

    fun disableBiometric() {
        settings.putBool(KEY_BIOMETRIC_ENABLED, false)
        _isLocked = false
    }

    /** Llamado cuando la app pasa a background (ON_STOP / sceneDidEnterBackground) */
    fun onAppBackground() {
        if (biometricEnabled) _isLocked = true
    }

    /** Llamado al arrancar la app — bloquea si la biometría está activada */
    fun onAppStart() {
        if (biometricEnabled) _isLocked = true
    }

    /** Llamado cuando la autenticación es exitosa */
    fun onUnlocked() {
        _isLocked = false
    }
}
