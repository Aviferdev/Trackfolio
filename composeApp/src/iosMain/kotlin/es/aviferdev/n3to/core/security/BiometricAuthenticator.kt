package es.aviferdev.n3to.core.security

import kotlinx.cinterop.ExperimentalForeignApi
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics

actual class BiometricAuthenticator {

    @OptIn(ExperimentalForeignApi::class)
    actual fun isAvailable(): Boolean =
        LAContext().canEvaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = null
        )

    actual fun authenticate(
        title: String,
        subtitle: String,
        onResult: (BiometricResult) -> Unit
    ) {
        LAContext().evaluatePolicy(
            LAPolicyDeviceOwnerAuthentication,
            localizedReason = title
        ) { success, error ->
            if (success) {
                onResult(BiometricResult.Success)
            } else {
                error?.let {
                    val code = it.code.toInt()
                    onResult(
                        when (code) {
                            -2, -3, -4 -> BiometricResult.UserCancelled
                            -6, -7, -8 -> BiometricResult.NotAvailable
                            else       -> BiometricResult.Error(it.localizedDescription)
                        }
                    )
                } ?: onResult(BiometricResult.Error("Unknown error"))
            }
        }
    }
}
