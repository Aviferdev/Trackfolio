package es.aviferdev.n3to.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.CyanSubtle
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Slide 3 — Inversiones con FIFO.
 */
@Composable
fun SlidePortfolio(modifier: Modifier = Modifier) {

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = EaseInOutCubic)
    )

    val lineProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(250)
        lineProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = EaseInOutCubic)
        )
    }

    var pill1Visible by remember { mutableStateOf(false) }
    var pill2Visible by remember { mutableStateOf(false) }
    var pill3Visible by remember { mutableStateOf(false) }
    var calloutVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300); pill1Visible = true
        delay(80);  pill2Visible = true
        delay(80);  pill3Visible = true
        delay(100); calloutVisible = true
    }

    val pill1Alpha by animateFloatAsState(targetValue = if (pill1Visible) 1f else 0f, animationSpec = tween(500, easing = EaseInOutCubic))
    val pill2Alpha by animateFloatAsState(targetValue = if (pill2Visible) 1f else 0f, animationSpec = tween(500, easing = EaseInOutCubic))
    val pill3Alpha by animateFloatAsState(targetValue = if (pill3Visible) 1f else 0f, animationSpec = tween(500, easing = EaseInOutCubic))
    val calloutAlpha by animateFloatAsState(targetValue = if (calloutVisible) 1f else 0f, animationSpec = tween(500, easing = EaseInOutCubic))

    data class StockPill(val ticker: String, val returnPct: String, val badgeColor: Color)

    // Colores visibles sobre fondo navy para cada ticker
    val stocks = listOf(
        StockPill("AAPL", "+13,8%", CyanSubtle),
        StockPill("NVDA", "+36,4%", Color(0xFF76B900)),
        StockPill("MSFT", "+9,0%",  Color(0xFF00A4EF))
    )
    val pillAlphas = listOf(pill1Alpha, pill2Alpha, pill3Alpha)
    val pathLength = 300f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // ── Portfolio card ───────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = cardAlpha }
                .background(color = NavySurface, shape = RoundedCornerShape(16.dp))
                .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VALOR PORTFOLIO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextTertiary,
                        letterSpacing = 0.7.sp
                    )
                    Text(text = "+5,6%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "41.409,90 €",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = (-1).sp
                )

                Spacer(Modifier.height(14.dp))

                Canvas(modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    val cvW = size.width
                    val cvH = size.height
                    val scaleX = cvW / 200f
                    val scaleY = cvH / 60f

                    val rawPoints = listOf(
                        Offset(0f, 45f), Offset(20f, 38f), Offset(40f, 42f),
                        Offset(60f, 30f), Offset(80f, 33f), Offset(100f, 22f),
                        Offset(120f, 26f), Offset(140f, 15f), Offset(160f, 18f),
                        Offset(180f, 8f), Offset(200f, 12f)
                    )
                    val scaledPoints = rawPoints.map { Offset(it.x * scaleX, it.y * scaleY) }

                    val chartPath = Path().apply {
                        scaledPoints.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
                    }
                    val areaPath = Path().apply {
                        addPath(chartPath)
                        lineTo(scaledPoints.last().x, cvH)
                        lineTo(scaledPoints.first().x, cvH)
                        close()
                    }

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(CyanAccent.copy(alpha = 0.20f), CyanAccent.copy(alpha = 0f)),
                            startY = 0f, endY = cvH
                        )
                    )
                    drawPath(
                        path = chartPath,
                        color = CyanAccent,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(pathLength, pathLength),
                                phase = pathLength * (1f - lineProgress.value)
                            )
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Stock pills ──────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            stocks.forEachIndexed { index, stock ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { alpha = pillAlphas[index] }
                        .background(color = NavySurface, shape = RoundedCornerShape(12.dp))
                        .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(color = stock.badgeColor.copy(alpha = 0.13f), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, stock.badgeColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stock.ticker, fontSize = 8.sp, fontWeight = FontWeight.Black, color = stock.badgeColor)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(text = stock.returnPct, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IncomeGreen, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── FIFO callout ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = calloutAlpha }
                .background(color = CyanAccent.copy(alpha = 0.10f), shape = RoundedCornerShape(11.dp))
                .border(1.dp, NavyBorder, RoundedCornerShape(11.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🧮", fontSize = 14.sp, color = Color.Unspecified)
                Spacer(Modifier.width(8.dp))
                Text(text = "Lotes FIFO automáticos", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.weight(1f))
                Text(text = "Plusvalías reales", fontSize = 10.sp, color = TextTertiary)
            }
        }
    }
}

@Preview
@Composable
private fun SlidePortfolioPreview() {
    N3toTheme {
        SlidePortfolio()
    }
}
