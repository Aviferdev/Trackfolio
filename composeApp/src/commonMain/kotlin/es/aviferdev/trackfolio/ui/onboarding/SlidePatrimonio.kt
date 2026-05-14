package es.aviferdev.trackfolio.ui.onboarding

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.BorderGray2
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Slide 2 — Tu patrimonio, claro.
 *
 * Muestra una tarjeta principal ligeramente rotada con el efectivo total
 * y el disponible neto. Dos cards flotantes laterales: Portfolio (+verde)
 * e Hipoteca (−rojo).
 */
@Composable
fun SlidePatrimonio(modifier: Modifier = Modifier) {

    // ── Entrance animation ───────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = EaseInOutCubic)
    )

    // ── Floating animations ──────────────────────────────
    val infiniteTransition = rememberInfiniteTransition()

    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )

    val floatOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        // ── Main rotated card ────────────────────────────
        Box(
            modifier = Modifier
                .width(260.dp)
                .graphicsLayer { alpha = cardAlpha }
                .rotate(-3f)
                .background(
                    color = PrimaryDark,
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 26.dp, vertical = 24.dp)
        ) {
            Column {
                // "EFECTIVO TOTAL" label
                Text(
                    text = "EFECTIVO TOTAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.55f),
                    letterSpacing = 0.7.sp
                )

                Spacer(Modifier.height(8.dp))

                // Big amount + currency inline
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "12.340,50",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-1.6).sp,
                        lineHeight = 42.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "€",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Divider
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.25f))
                )

                Spacer(Modifier.height(10.dp))

                // "Disponible con deudas" label
                Text(
                    text = "Disponible con deudas",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(3.dp))

                // Net amount
                Text(
                    text = "11.890,50 €",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // ── Floating card: Portfolio (top right) ─────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = 24.dp)
                .graphicsLayer { translationY = floatOffset2 }
                .background(
                    color = SurfaceWhite,
                    shape = RoundedCornerShape(14.dp)
                )
                .border(1.dp, BorderGray2, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = "📈 Portfolio",
                    fontSize = 14.sp,
                    color = Color.Unspecified
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "+2.209 €",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = IncomeGreen
                )
            }
        }

        // ── Floating card: Hipoteca (bottom left) ────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = (-24).dp)
                .graphicsLayer { translationY = floatOffset3 }
                .background(
                    color = SurfaceWhite,
                    shape = RoundedCornerShape(14.dp)
                )
                .border(1.dp, BorderGray2, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = "🏠 Hipoteca",
                    fontSize = 14.sp,
                    color = Color.Unspecified
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "−142.300 €",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ExpenseRed
                )
            }
        }
    }
}

@Preview
@Composable
private fun SlidePatrimonioPreview() {
    TrackfolioTheme {
        SlidePatrimonio()
    }
}
