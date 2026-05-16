package es.aviferdev.n3to.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.BackgroundGray
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Pantalla de splash mostrada al abrir la aplicación.
 *
 * Muestra el icono vectorial de N3to centrado con una animación de entrada
 * escalonada: primero el icono, luego el nombre y tagline, y finalmente
 * el loader de carga. Todo sobre fondo oscuro con fade out final.
 *
 * @param minDisplayTimeMs Duración mínima de visualización en milisegundos (default 1500ms).
 * @param onSplashFinished Callback invocado cuando el splash ha completado su animación de salida.
 */
@Composable
fun SplashScreen(
    minDisplayTimeMs: Long = 1500L,
    onSplashFinished: () -> Unit
) {
    // ── Estados de visibilidad ────────────────────────────
    var splashVisible by remember { mutableStateOf(true) }
    var markVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }
    var loaderVisible by remember { mutableStateOf(false) }

    // ── Animaciones de entrada (stagger) ──────────────────
    val markAlpha by animateFloatAsState(
        targetValue = if (markVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (textVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )
    val loaderAlpha by animateFloatAsState(
        targetValue = if (loaderVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400)
    )

    // ── Secuencia de temporización ────────────────────────
    LaunchedEffect(Unit) {
        // Stagger de entrada
        delay(50L)
        markVisible = true

        delay(250L)
        textVisible = true

        delay(300L)
        loaderVisible = true

        // Tiempo mínimo de visualización total
        val staggerElapsed = 600L // 50 + 250 + 300
        if (minDisplayTimeMs > staggerElapsed) {
            delay(minDisplayTimeMs - staggerElapsed)
        }

        // Fade out
        splashVisible = false
        delay(400L)
        onSplashFinished()
    }

    // ── UI ────────────────────────────────────────────────
    AnimatedVisibility(
        visible = splashVisible,
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Icono vectorial de N3to
                Box(modifier = Modifier.alpha(markAlpha)) {
                    N3toMark()
                }

                // Nombre de la app + tagline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.alpha(textAlpha)
                ) {
                    Text(
                        text = "N3to",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        letterSpacing = (-0.64).sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Track every move.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextTertiary,
                        letterSpacing = 0.26.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Loader de 3 dots en la parte inferior
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp)
                    .alpha(loaderAlpha)
            ) {
                SplashLoader()
            }
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreen(onSplashFinished = {})
}
