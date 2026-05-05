package es.aviferdev.trackfolio.security

import kotlinx.cinterop.ExperimentalForeignApi
import platform.LocalAuthentication.LAContext
import platform.Foundation.NSError
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics

actual class BiometricAuthenticator {

    @OptIn(ExperimentalForeignApi::class)
    actual fun isAvailable(): Boolean {
        val context = LAContext()
        var error: NSError? = null
        return context.canEvaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = null
        )
    }

    actual fun authenticate(
        title: String,
        subtitle: String,
        onResult: (BiometricResult) -> Unit
    ) {
        val context = LAContext()
        context.evaluatePolicy(
            // Permite Face ID, Touch ID y código PIN como fallback
            LAPolicyDeviceOwnerAuthentication,
            localizedReason = title
        ) { success, error ->
            if (success) {
                onResult(BiometricResult.Success)
            } else {
                val nsError = error
                if (nsError != null) {
                    // LAErrorUserCancel = -2, LAErrorUserFallback = -3, LAErrorSystemCancel = -4
                    val code = nsError.code.toInt()
                    onResult(
                        when (code) {
                            -2, -3, -4 -> BiometricResult.UserCancelled
                            -6, -7, -8 -> BiometricResult.NotAvailable  // LAErrorBiometryNotAvailable/NotEnrolled/Lockout
                            else       -> BiometricResult.Error(nsError.localizedDescription)
                        }
                    )
                } else {
                    onResult(BiometricResult.Error("Unknown error"))
                }
            }
        }
    }
}
