package es.aviferdev.n3to.core.browser

import androidx.compose.runtime.Composable

/**
 * Interfaz funcional para abrir URLs dentro de la app,
 * usando Chrome Custom Tab en Android y el navegador del sistema en iOS.
 */
fun interface UrlOpener {
    fun openUrl(url: String)
}

/**
 * Crea un [UrlOpener] específico de la plataforma.
 *
 * - **Android:** Utiliza [androidx.browser.customtabs.CustomTabsIntent]
 *   para abrir la URL en un Chrome Custom Tab dentro de la app.
 * - **iOS:** Utiliza [androidx.compose.ui.platform.LocalUriHandler]
 *   para delegar la apertura al navegador del sistema como fallback.
 *
 * @return Una lambda [UrlOpener] preparada para abrir URLs.
 */
@Composable
expect fun rememberUrlOpener(): UrlOpener
