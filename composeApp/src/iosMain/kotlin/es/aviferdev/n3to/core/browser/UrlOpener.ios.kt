package es.aviferdev.n3to.core.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler

/**
 * Implementación iOS de [rememberUrlOpener].
 *
 * En iOS, por limitaciones de la plataforma desde Kotlin/Native,
 * se utiliza [LocalUriHandler] que abre la URL en Safari (navegador del sistema).
 *
 * @todo Implementar SFSafariViewController para una experiencia in-app en iOS.
 */
@Composable
actual fun rememberUrlOpener(): UrlOpener {
    val uriHandler = LocalUriHandler.current
    return remember {
        UrlOpener { url ->
            uriHandler.openUri(url)
        }
    }
}
