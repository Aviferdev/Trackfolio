package es.aviferdev.trackfolio.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

// TODO Refactorizar

actual class BiometricAuthenticator(private val context: Context) {

    private var activityRef: WeakReference<FragmentActivity>? = null

    fun bindActivity(activity: FragmentActivity) {
        activityRef = WeakReference(activity)
    }

    fun unbindActivity() {
        activityRef = null
    }

    private fun currentActivity(): FragmentActivity? = activityRef?.get()

    actual fun isAvailable(): Boolean =
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) ==
                BiometricManager.BIOMETRIC_SUCCESS

    actual fun authenticate(
        title: String,
        subtitle: String,
        onResult: (BiometricResult) -> Unit
    ) {
        val activity = currentActivity()
        if (activity == null) {
            onResult(BiometricResult.Error("La aplicación no está en primer plano"))
            return
        }
        if (activity.isFinishing || activity.isDestroyed) {
            onResult(BiometricResult.Error("La actividad no está disponible"))
            return
        }
        try {
            BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onResult(BiometricResult.Success)
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        onResult(
                            when (errorCode) {
                                BiometricPrompt.ERROR_USER_CANCELED,
                                BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BiometricResult.UserCancelled
                                BiometricPrompt.ERROR_HW_NOT_PRESENT,
                                BiometricPrompt.ERROR_HW_UNAVAILABLE,
                                BiometricPrompt.ERROR_NO_BIOMETRICS  -> BiometricResult.NotAvailable
                                else -> BiometricResult.Error(errString.toString())
                            }
                        )
                    }
                    override fun onAuthenticationFailed() {
                        // No emitimos resultado aquí — el sistema muestra el error automáticamente
                    }
                }
            ).authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                    .build()
            )
        } catch (e: Exception) {
            onResult(BiometricResult.Error("Error al iniciar autenticación: ${e.localizedMessage ?: e.message}"))
        }
    }
}
