package es.aviferdev.n3to.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.BackgroundGray
import es.aviferdev.n3to.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Pantalla de splash mostrada al abrir la aplicación.
 *
 * Muestra el icono de la app centrado sobre fondo oscuro durante un tiempo mínimo
 * configurable y luego realiza un fade out para transicionar al contenido principal.
 *
 * @param minDisplayTimeMs Duración mínima de visualización en milisegundos (default 1500ms).
 * @param onSplashFinished Callback invocado cuando el splash ha completado su animación de salida.
 */
@Composable
fun SplashScreen(
    minDisplayTimeMs: Long = 1500L,
    onSplashFinished: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Tiempo mínimo de visualización del splash
        delay(minDisplayTimeMs)
        // Iniciar fade out
        visible = false
        // Esperar a que termine la animación de salida
        delay(400L)
        // Notificar que el splash terminó
        onSplashFinished()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono de la app
                androidx.compose.foundation.Image(
                    painter = painterResource(Res.drawable.app_icon),
                    contentDescription = "N3to",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Nombre de la aplicación
                Text(
                    text = "N3to",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreen(onSplashFinished = {})
}
