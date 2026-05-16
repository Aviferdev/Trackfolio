package es.aviferdev.trackfolio.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradient
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Radio del halo detrás de la card. */
private const val HALO_RADIUS_PX = 480f

// ─── Constantes de colores del diseño ─────────────────────────────────────────
private val CardBorder = BorderGray
private val CardBg = SurfaceWhite
private val BadgeBg = Surface3
private val AccentGradient = Brush.verticalGradient(
    0f to PrimaryDark,
    1f to Color(0xFF8B89F8)
)
private val PillBg = SurfaceWhite
private val PillBorder = BorderGray
private val HintBg = Color(0x335B57F5)  // PrimaryAlpha
private val HintBorder = Color(0x405B57F5) // PrimaryDark al 25%

// ─── Animación del dot pulsante ──────────────────────────────────────────────
private val PulseSpec: androidx.compose.animation.core.InfiniteRepeatableSpec<Float> = infiniteRepeatable(
    animation = tween<Float>(1200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
    repeatMode = RepeatMode.Reverse
)

@Composable
fun OnboardingRealEstatePage(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Halo (se dibuja detrás de todo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f),
            contentAlignment = Alignment.Center
        ) {
            // No dibujamos el halo como Box porque no tenemos acceso a drawBehind
            // Lo simulamos con un Box transparente que contiene el contenido
        }

        // ── Pill indicador ────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(400)) +
                    slideInVertically(
                        animationSpec = tween(400),
                        initialOffsetY = { it / 4 }
                    )
        ) {
            SectionPill()
        }

        Spacer(Modifier.height(20.dp))

        // ── Card principal ────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 150)) +
                    slideInVertically(
                        animationSpec = tween(400, delayMillis = 150),
                        initialOffsetY = { it / 4 }
                    )
        ) {
            ViviendaMockCard()
        }

        Spacer(Modifier.height(16.dp))

        // ── Hint pill ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 350)) +
                    slideInVertically(
                        animationSpec = tween(400, delayMillis = 350),
                        initialOffsetY = { it / 4 }
                    )
        ) {
            HintPill()
        }
    }
}

// ─── Pill "SECCIÓN · PATRIMONIO" ──────────────────────────────────────────────
@Composable
private fun SectionPill() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 1f, targetValue = 0.35f,
        animationSpec = PulseSpec, label = "pulseAlpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(PillBg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Dot pulsante
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(PrimaryDark.copy(alpha = pulseAlpha))
        )
        Text(
            text = "SECCIÓN · PATRIMONIO",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = TextSecondary
        )
    }
}

// ─── Card mockup de vivienda ──────────────────────────────────────────────────
@Composable
private fun ViviendaMockCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .padding(18.dp)
    ) {
        Column {
            // Fila 1: icono + título + badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icono círculo gradiente
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(AccentGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\uD83C\uDFE0", fontSize = 22.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Mi vivienda",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Activo inmobiliario",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                }

                // Badge EJEMPLO
                Text(
                    text = "EJEMPLO",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = TextTertiary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BadgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDividerCustom()
            Spacer(Modifier.height(12.dp))

            // Grid 2 columnas: VALOR ESTIMADO | HIPOTECA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "VALOR ESTIMADO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = TextTertiary
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "285.000 €",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp,
                        color = TextPrimary
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "HIPOTECA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = TextTertiary
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "−142.300 €",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp,
                        color = ExpenseRed
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDividerCustom()
            Spacer(Modifier.height(12.dp))

            // Fila final: "Aporta a tu patrimonio" + neto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Aporta a tu patrimonio",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Text(
                    text = "+142.700 €",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = IncomeGreen
                )
            }
        }
    }
}

// ─── Hint pill ────────────────────────────────────────────────────────────────
@Composable
private fun HintPill() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HintBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "\u2728", fontSize = 16.sp)
        Text(
            text = buildAnnotatedString {
                append("Cuando entres, podrás registrarla desde ")
                withStyle(SpanStyle(color = PrimaryDark, fontWeight = FontWeight.ExtraBold)) {
                    append("Patrimonio")
                }
                append(".")
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            lineHeight = 15.sp
        )
    }
}

// ─── Divisor personalizado ────────────────────────────────────────────────────
@Composable
private fun HorizontalDividerCustom() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderGray)
    )
}

@Preview
@Composable
private fun OnboardingRealEstatePagePreview() {
    TrackfolioTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray),
            contentAlignment = Alignment.Center
        ) {
            OnboardingRealEstatePage(isVisible = true)
        }
    }
}
