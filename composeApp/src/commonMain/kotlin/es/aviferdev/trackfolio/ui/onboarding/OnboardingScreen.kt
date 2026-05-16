package es.aviferdev.trackfolio.ui.onboarding

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import es.aviferdev.trackfolio.ui.theme.BackgroundGray
import es.aviferdev.trackfolio.ui.theme.BorderGray2
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

// ─── ViewModel ─────────────────────────────────────────────

class OnboardingViewModel(
    private val onComplete: () -> Unit
) : ViewModel() {

    fun complete() {
        onComplete()
    }
}

// ─── Slide metadata ────────────────────────────────────────

private data class SlideInfo(
    val id: String,
    val title: String,
    val body: String,
    val hideText: Boolean = false
)

private val SLIDES = listOf(
    SlideInfo(
        id = "welcome",
        title = "Bienvenido a Trackfolio",
        body = "Tu vida financiera, organizada. Movimientos, inversiones y patrimonio en un solo lugar.",
        hideText = true
    ),
    SlideInfo(
        id = "patrim",
        title = "Tu patrimonio, claro",
        body = "Visualiza tu efectivo y tu disponible neto con deudas. Activos y pasivos al mismo nivel."
    ),
    SlideInfo(
        id = "portfolio",
        title = "Inversiones con FIFO",
        body = "Compras, ventas, dividendos y lotes FIFO calculados automáticamente para tus plusvalías."
    ),
    SlideInfo(
        id = "movim",
        title = "Movimientos con sentido",
        body = "Categorías inteligentes y campos específicos por tipo: alquileres, gasolina, nóminas, facturas…"
    ),
    SlideInfo(
        id = "realestate",
        title = "Registra tu vivienda",
        body = "Añade tu vivienda al patrimonio desde la sección Patrimonio. Vincula tu hipoteca para un cálculo preciso y registra los ingresos y gastos del alquiler."
    ),
    SlideInfo(
        id = "fiscal",
        title = "Informe fiscal listo",
        body = "Trackfolio prepara automáticamente tu base imponible, IRPF y ganancias patrimoniales."
    )
)

// ─── Screen (entry point) ──────────────────────────────────

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val viewModel = remember { OnboardingViewModel(onComplete) }
    val pagerState = rememberPagerState(pageCount = { SLIDES.size })
    var currentSlide by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Sync pager swipe → currentSlide
    LaunchedEffect(pagerState.currentPage) {
        currentSlide = pagerState.currentPage
    }

    val isLast = currentSlide == SLIDES.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar: Skip button ─────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, end = 20.dp)
            ) {
                if (!isLast) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(SLIDES.size - 1)
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(
                            text = "Saltar",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── Pager ────────────────────────────────────
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                SlidePage(slide = SLIDES[page])
            }

            // ── Bottom controls ──────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Page dots
                OnboardingDots(
                    totalDots = SLIDES.size,
                    selectedIndex = currentSlide
                )

                Spacer(Modifier.height(18.dp))

                // CTA buttons
                if (isLast) {
                    Button(
                        onClick = { viewModel.complete() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryDark
                        )
                    ) {
                        Text(
                            text = "Comenzar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentSlide > 0) {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(currentSlide - 1)
                                    }
                                }
                            ) {
                                Text(
                                    text = "Atrás",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Spacer(Modifier.width(1.dp))
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(currentSlide + 1)
                                }
                            },
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryDark
                            ),
                            modifier = Modifier.height(44.dp),
                            contentPadding = PaddingValues(horizontal = 28.dp)
                        ) {
                            Text(
                                text = "Continuar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ─── Slide page wrapper ────────────────────────────────────

@Composable
private fun SlidePage(slide: SlideInfo) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        // Visual area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (slide.hideText) 1f else 0.55f),
            contentAlignment = Alignment.Center
        ) {
            when (slide.id) {
                "welcome"   -> SlideWelcome()
                "patrim"    -> SlidePatrimonio()
                "portfolio" -> SlidePortfolio()
                "movim"     -> SlideMovimientos()
                "fiscal"    -> SlideFiscal()
            }
        }

        // Text area (hidden for welcome slide — text is centered in visual)
        if (!slide.hideText) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = slide.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = (-1).sp,
                    lineHeight = 30.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = slide.body,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

// ─── Dots indicator ────────────────────────────────────────

@Composable
private fun OnboardingDots(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .width(if (isSelected) 22.dp else 6.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isSelected) PrimaryDark else BorderGray2)
            )
        }
    }
}

// ─── Preview ───────────────────────────────────────────────

@Preview
@Composable
private fun OnboardingScreenPreview() {
    TrackfolioTheme {
        OnboardingScreen(onComplete = {})
    }
}
