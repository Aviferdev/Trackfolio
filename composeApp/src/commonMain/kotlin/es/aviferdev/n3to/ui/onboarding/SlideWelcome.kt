package es.aviferdev.n3to.ui.onboarding

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.CyanGlow
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.NavyDeep
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.app_icon
import n3to.composeapp.generated.resources.onboarding_welcome_subtitle
import n3to.composeapp.generated.resources.onboarding_welcome_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.min

/**
 * Slide 1 — Bienvenido a N3to.
 */
@Composable
fun SlideWelcome(modifier: Modifier = Modifier) {

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val iconAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = EaseInOutCubic)
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 550, delayMillis = 100, easing = EaseInOutCubic)
    )
    val bodyAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 550, delayMillis = 200, easing = EaseInOutCubic)
    )

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
        Box(
            modifier = Modifier.size(380.dp),
            contentAlignment = Alignment.Center
        ) {
            // Halo cian radial (coherente con el orb decorativo de la Home/Splash)
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
                                    CyanGlow.copy(alpha = 0.25f),
                                    CyanGlow.copy(alpha = 0f)
                                ),
                                center = center,
                                radius = gradientRadius
                            ),
                            radius = side / 2f,
                            center = center
                        )
                    }
            )

            Box(
                modifier = Modifier
                    .size(128.dp)
                    .graphicsLayer {
                        translationY = floatOffset
                        alpha = iconAlpha
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val cornerRadiusPx = size.width * 225f / 1024f
                            drawRoundRect(
                                color = NavyDeep,
                                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                size = size
                            )
                        }
                )
                Image(
                    painter = painterResource(Res.drawable.app_icon),
                    contentDescription = stringResource(Res.string.onboarding_welcome_title),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.onboarding_welcome_title),
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            letterSpacing = (-1.2).sp,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
            modifier = Modifier.graphicsLayer { alpha = titleAlpha }
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(Res.string.onboarding_welcome_subtitle),
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
    N3toTheme {
        SlideWelcome()
    }
}
