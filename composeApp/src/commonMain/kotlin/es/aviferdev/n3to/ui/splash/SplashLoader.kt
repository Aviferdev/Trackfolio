package es.aviferdev.n3to.ui.splash

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import es.aviferdev.n3to.ui.theme.TextPrimary
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Loader animado con 3 dots que parpadean y escalan secuencialmente.
 *
 * Cada dot tiene un offset de 180ms respecto al anterior, creando un efecto
 * de "ola" continua. Inspirado en el diseño splash.jsx.
 *
 * @param color Color de los dots (default TextPrimary).
 * @param modifier Modifier adicional.
 */
@Composable
fun SplashLoader(
    color: Color = TextPrimary.copy(alpha = 0.55f),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..2) {
            val delayMs = i * 180

            val animProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1200,
                        delayMillis = delayMs,
                        easing = EaseInOutCubic
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )

            // Opacidad: 0.25 → 1.0 → 0.25
            val alpha = lerp(0.25f, 1f, animProgress)
            // Escala: 1.0 → 1.15 → 1.0
            val scale = lerp(1f, 1.15f, animProgress)

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(alpha)
                    .scale(scale)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}

/**
 * Interpolación lineal simple entre [start] y [end] usando [fraction] (0..1).
 */
private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

@Preview
@Composable
private fun SplashLoaderPreview() {
    SplashLoader()
}
