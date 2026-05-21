package es.aviferdev.n3to.ui.onboarding

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.N3toTheme

import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.onboarding_disponible_deudas
import n3to.composeapp.generated.resources.onboarding_efectivo_total
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Slide 2 — Tu patrimonio, claro.
 */
@Composable
fun SlidePatrimonio(modifier: Modifier = Modifier) {
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val heroCardBg2 = MaterialTheme.appColors.heroCardEnd
    val appCCyanSubtle = MaterialTheme.appColors.cyanSubtle
    val appCNavyBorder = MaterialTheme.appColors.navyBorder

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = EaseInOutCubic)
    )

    val infiniteTransition = rememberInfiniteTransition()

    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -5f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutCubic), RepeatMode.Reverse)
    )
    val floatOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -5f,
        animationSpec = infiniteRepeatable(tween(3600, easing = EaseInOutCubic), RepeatMode.Reverse)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        // ── Main card — gradiente diagonal idéntico a HeroCard ───────────
        Box(
            modifier = Modifier
                .width(260.dp)
                .graphicsLayer { alpha = cardAlpha }
                .rotate(-3f)
                .clip(RoundedCornerShape(22.dp))
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(heroCardBg1, heroCardBg2),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )
                }
                .padding(horizontal = 26.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.onboarding_efectivo_total),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.55f),
                    letterSpacing = 0.7.sp
                )

                Spacer(Modifier.height(8.dp))

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

                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )
                Spacer(Modifier.height(10.dp))

                Text(
                    text = stringResource(Res.string.onboarding_disponible_deudas),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(3.dp))
                // appCCyanSubtle para el valor secundario, igual que HeroCard
                Text(
                    text = "11.890,50 €",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = appCCyanSubtle,
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
                .background(color = heroCardBg1, shape = RoundedCornerShape(14.dp))
                .border(1.dp, appCNavyBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column {
                Text(text = "📈 Portfolio", fontSize = 14.sp, color = Color.Unspecified)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "+2.209 €",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.appColors.income
                )
            }
        }

        // ── Floating card: Hipoteca (bottom left) ────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = (-24).dp)
                .graphicsLayer { translationY = floatOffset3 }
                .background(color = heroCardBg1, shape = RoundedCornerShape(14.dp))
                .border(1.dp, appCNavyBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column {
                Text(text = "🏠 Hipoteca", fontSize = 14.sp, color = Color.Unspecified)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "−142.300 €",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.appColors.expense
                )
            }
        }
    }
}

@Preview
@Composable
private fun SlidePatrimonioPreview() {
    N3toTheme {
        SlidePatrimonio()
    }
}
