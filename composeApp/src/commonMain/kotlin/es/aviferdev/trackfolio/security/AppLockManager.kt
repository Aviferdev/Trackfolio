package es.aviferdev.trackfolio.security

/**
 * Gestiona el estado de bloqueo de la app y las preferencias de biometría.
 * Singleton Koin (single).
 */
class AppLockManager {
    /** True cuando la app está bloqueada y requiere autenticación */
    private var _isLocked = false
    val isLocked: Boolean get() = _isLocked

    /** True si el usuario ha activado el bloqueo biométrico en Ajustes */
    private var _biometricEnabled = false
    val biometricEnabled: Boolean get() = _biometricEnabled

    fun enableBiometric() {
        _biometricEnabled = true
    }

    fun disableBiometric() {
        _biometricEnabled = false
    }

    /** Llamado cuando la app pasa a background */
    fun onAppBackground() {
        if (_biometricEnabled) _isLocked = true
    }

    /** Llamado cuando la autenticación es exitosa */
    fun onUnlocked() {
        _isLocked = false
    }
}
