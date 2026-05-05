package es.aviferdev.trackfolio.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

actual class BiometricAuthenticator(private val context: Context) {

    // Referencia débil a la Activity (FragmentActivity, requerida por BiometricPrompt).
    // MainActivity la registra/desregistra en onCreate/onDestroy.
    private var activityRef: WeakReference<FragmentActivity>? = null

    fun bindActivity(activity: FragmentActivity) {
        activityRef = WeakReference(activity)
    }

    fun unbindActivity() {
        activityRef = null
    }

    private fun currentActivity(): FragmentActivity? = activityRef?.get()

    actual fun isAvailable(): Boolean {
        val manager = BiometricManager.from(context)
        // Aceptamos cualquiera de los dos: biometría fuerte o credencial del dispositivo
        // (PIN/patrón/contraseña). Si el usuario al menos tiene PIN, podrá desbloquear.
        return manager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    actual fun authenticate(
        title: String,
        subtitle: String,
        onResult: (BiometricResult) -> Unit
    ) {
        val activity = currentActivity() ?: run {
            onResult(BiometricResult.Error("La aplicación no está en primer plano"))
            return
        }

        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
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

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        prompt.authenticate(promptInfo)
    }
}
