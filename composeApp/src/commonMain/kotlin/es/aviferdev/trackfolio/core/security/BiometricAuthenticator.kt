package es.aviferdev.trackfolio.core.security

/**
 * Resultado de un intento de autenticación biométrica.
 */
sealed class BiometricResult {
    data object Success : BiometricResult()
    data object UserCancelled : BiometricResult()
    data object NotAvailable : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}

/**
 * Contrato multiplataforma para biometría.
 * expect/actual: iOS usa LocalAuthentication, Android usa BiometricPrompt.
 */
expect class BiometricAuthenticator {
    /** True si el dispositivo tiene biometría configurada y disponible */
    fun isAvailable(): Boolean

    /**
     * Solicita autenticación biométrica.
     * [onResult] se invoca en el hilo principal con el resultado.
     */
    fun authenticate(
        title: String,
        subtitle: String,
        onResult: (BiometricResult) -> Unit
    )
}
