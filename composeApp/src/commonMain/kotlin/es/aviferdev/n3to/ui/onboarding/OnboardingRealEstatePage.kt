package es.aviferdev.n3to.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.NavyDeep
import es.aviferdev.n3to.ui.theme.NavySelected
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.NavySurfaceLight
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.networth_title
import n3to.composeapp.generated.resources.onboarding_seccion_patrimonio
import n3to.composeapp.generated.resources.realestate_detail_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

// ─── Constantes de colores del sistema navy ─────────────────
private val CardBorder    = NavyBorder
private val CardBg        = NavySurface
private val BadgeBg       = NavySurfaceLight
private val PillBg        = NavySurface
private val PillBorder    = NavyBorder
private val HintBg        = CyanAccent.copy(alpha = 0.10f)
private val HintBorder    = NavyBorder
private val AccentGradient = Brush.verticalGradient(
    0f to NavySelected,
    1f to NavySurface
)

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
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(400)) +
                    slideInVertically(animationSpec = tween(400), initialOffsetY = { it / 4 })
        ) {
            SectionPill()
        }

        Spacer(Modifier.height(20.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 150)) +
                    slideInVertically(animationSpec = tween(400, delayMillis = 150), initialOffsetY = { it / 4 })
        ) {
            ViviendaMockCard()
        }

        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 350)) +
                    slideInVertically(animationSpec = tween(400, delayMillis = 350), initialOffsetY = { it / 4 })
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
            .border(1.dp, PillBorder, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(CyanAccent.copy(alpha = pulseAlpha))
        )
        Text(
            text = stringResource(Res.string.onboarding_seccion_patrimonio),
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
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                // Icono gradiente navy
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(AccentGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🏠", fontSize = 22.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(text = "Mi vivienda", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "Activo inmobiliario", fontSize = 10.sp, color = TextTertiary)
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(text = "VALOR ESTIMADO", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = TextTertiary)
                    Spacer(Modifier.height(3.dp))
                    Text(text = "285.000 €", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.4).sp, color = TextPrimary)
                }
                Column(Modifier.weight(1f)) {
                    Text(text = "HIPOTECA", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = TextTertiary)
                    Spacer(Modifier.height(3.dp))
                    Text(text = "−142.300 €", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.4).sp, color = ExpenseRed)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDividerCustom()
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(text = "Aporta a tu patrimonio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Text(text = "+142.700 €", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp, color = IncomeGreen)
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
            .border(1.dp, HintBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "✨", fontSize = 16.sp)
        Text(
            text = buildAnnotatedString {
                append("Cuando entres, podrás registrarla desde ")
                withStyle(SpanStyle(color = CyanAccent, fontWeight = FontWeight.ExtraBold)) {
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

// ─── Divisor ──────────────────────────────────────────────────────────────────
@Composable
private fun HorizontalDividerCustom() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NavyBorder))
}

@Preview
@Composable
private fun OnboardingRealEstatePagePreview() {
    N3toTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(NavyDeep),
            contentAlignment = Alignment.Center
        ) {
            OnboardingRealEstatePage(isVisible = true)
        }
    }
}
