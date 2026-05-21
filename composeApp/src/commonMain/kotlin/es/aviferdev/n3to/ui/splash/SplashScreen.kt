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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.appColors
import kotlinx.coroutines.delay
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
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
                .background(MaterialTheme.appColors.navyDeep),
            contentAlignment = Alignment.Center
        ) {
            // Orb decorativo cian (esquina superior derecha, igual que HeroCard)
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 100.dp, y = (-100).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.appColors.cyanGlow.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

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
                        text = stringResource(Res.string.app_name),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.textPrimary,
                        letterSpacing = (-0.64).sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Track every move.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.appColors.textTertiary,
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
