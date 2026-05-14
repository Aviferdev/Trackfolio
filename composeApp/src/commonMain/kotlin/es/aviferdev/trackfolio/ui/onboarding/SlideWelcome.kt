package es.aviferdev.trackfolio.ui.onboarding

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.min

/**
 * Slide 1 — Bienvenido a Trackfolio.
 *
 * Muestra el icono de la app con halo radial degradado, animación de flotación,
 * título principal y descripción. Todo centrado (hideText = true en SLIDES).
 */
@Composable
fun SlideWelcome(modifier: Modifier = Modifier) {

    // ── Entrance animation state ─────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val iconAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = EaseInOutCubic)
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 550,
            delayMillis = 100,
            easing = EaseInOutCubic
        )
    )
    val bodyAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 550,
            delayMillis = 200,
            easing = EaseInOutCubic
        )
    )

    // ── Floating animation (infinite) ────────────────────
    val infiniteTransition = rememberInfiniteTransition()
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Halo + Icon ──────────────────────────────────
        Box(
            modifier = Modifier.size(380.dp),
            contentAlignment = Alignment.Center
        ) {
            // Radial halo using drawBehind
            Box(
                modifier = Modifier
                    .size(380.dp)
                    .drawBehind {
                        val side = min(size.width, size.height)
                        val center = Offset(side / 2f, side / 2f)
                        val gradientRadius = side * 0.5f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PrimaryDark.copy(alpha = 0.45f),
                                    PrimaryDark.copy(alpha = 0f)
                                ),
                                center = center,
                                radius = gradientRadius
                            ),
                            radius = side / 2f,
                            center = center
                        )
                    }
            )

            // App icon
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .graphicsLayer {
                        translationY = floatOffset
                        alpha = iconAlpha
                    }
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF1A1A3A), Color(0xFF0D0D0D)),
                            start = Offset(0f, 0f),
                            end = Offset(0f, Float.POSITIVE_INFINITY)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Chart drawn inside icon
                Canvas(modifier = Modifier.size(100.dp)) {
                    val scale = size.width / 200f

                    // Bottom bar
                    drawRoundRect(
                        color = PrimaryDark,
                        topLeft = Offset(50f * scale, 56f * scale),
                        size = androidx.compose.ui.geometry.Size(
                            100f * scale,
                            20f * scale
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            6f * scale
                        )
                    )

                    // Tall bar
                    drawRoundRect(
                        color = PrimaryDark,
                        topLeft = Offset(90f * scale, 56f * scale),
                        size = androidx.compose.ui.geometry.Size(
                            20f * scale,
                            88f * scale
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            6f * scale
                        )
                    )

                    // Line path
                    val linePath = Path().apply {
                        moveTo(56f * scale, 138f * scale)
                        lineTo(90f * scale, 116f * scale)
                        lineTo(110f * scale, 124f * scale)
                        lineTo(152f * scale, 78f * scale)
                    }
                    drawPath(
                        path = linePath,
                        color = Color.White,
                        style = Stroke(
                            width = 9f * scale,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // End dot
                    drawCircle(
                        color = Color.White,
                        radius = 7f * scale,
                        center = Offset(152f * scale, 78f * scale)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Title ────────────────────────────────────────
        Text(
            text = "Bienvenido a\nTrackfolio",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            letterSpacing = (-1.2).sp,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
            modifier = Modifier.graphicsLayer { alpha = titleAlpha }
        )

        Spacer(Modifier.height(12.dp))

        // ── Body ─────────────────────────────────────────
        Text(
            text = "Tu vida financiera, organizada. Movimientos, inversiones y patrimonio en un solo lugar.",
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 23.sp,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .graphicsLayer { alpha = bodyAlpha }
        )
    }
}

@Preview
@Composable
private fun SlideWelcomePreview() {
    TrackfolioTheme {
        SlideWelcome()
    }
}
