package es.aviferdev.n3to.core.browser

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Implementación Android de [rememberUrlOpener].
 *
 * Abre URLs utilizando [CustomTabsIntent] (Chrome Custom Tabs),
 * lo que permite navegar dentro de la app con la apariencia de Chrome.
 * Si Chrome no está disponible, fallback a [Intent.ACTION_VIEW].
 */
@Composable
actual fun rememberUrlOpener(): UrlOpener {
    val context = LocalContext.current
    return remember {
        UrlOpener { url ->
            try {
                val customTabsIntent = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setUrlBarHidingEnabled(true)
                    .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                    .build()
                customTabsIntent.launchUrl(context, Uri.parse(url))
            } catch (_: Exception) {
                // Fallback: abrir en navegador externo si Custom Tabs no está disponible
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }
}
