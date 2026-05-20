package es.aviferdev.n3to.ui.onboarding

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.usecase.onboarding.MarkOnboardingCompletedUseCase

import es.aviferdev.n3to.ui.theme.N3toTheme

import kotlinx.coroutines.launch
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.onboarding_continue
import n3to.composeapp.generated.resources.onboarding_skip
import n3to.composeapp.generated.resources.onboarding_start
import n3to.composeapp.generated.resources.onboarding_welcome_subtitle
import n3to.composeapp.generated.resources.onboarding_welcome_title
import n3to.composeapp.generated.resources.onboarding_slide_networth_title
import n3to.composeapp.generated.resources.onboarding_slide_portfolio_title
import n3to.composeapp.generated.resources.onboarding_slide_movements_title
import n3to.composeapp.generated.resources.onboarding_slide_fiscal_title
import n3to.composeapp.generated.resources.onboarding_slide_realestate_title
import n3to.composeapp.generated.resources.onboarding_welcome_desc
import n3to.composeapp.generated.resources.onboarding_patrimonio_desc
import n3to.composeapp.generated.resources.onboarding_portfolio_desc
import n3to.composeapp.generated.resources.onboarding_movements_desc
import n3to.composeapp.generated.resources.onboarding_fiscal_desc
import n3to.composeapp.generated.resources.onboarding_realestate_desc
import n3to.composeapp.generated.resources.common_back_cd
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

// ─── ViewModel ─────────────────────────────────────────────

class OnboardingViewModel(
    private val markCompleted: MarkOnboardingCompletedUseCase,
    private val onComplete: () -> Unit
) : ViewModel() {

    fun complete() {
        viewModelScope.launch {
            markCompleted()
            onComplete()
        }
    }
}

// ─── Slide metadata ────────────────────────────────────────

private data class SlideInfo(
    val id: String,
    val hideText: Boolean = false
)

private val SLIDES = listOf(
    SlideInfo(id = "welcome", hideText = true),
    SlideInfo(id = "patrim"),
    SlideInfo(id = "portfolio"),
    SlideInfo(id = "movim"),
    SlideInfo(id = "realestate"),
    SlideInfo(id = "fiscal")
)

// ─── Screen (entry point) ──────────────────────────────────

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val markCompleted = koinInject<MarkOnboardingCompletedUseCase>()
    val viewModel = remember { OnboardingViewModel(markCompleted, onComplete) }
    val pagerState = rememberPagerState(pageCount = { SLIDES.size })
    var currentSlide by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        currentSlide = pagerState.currentPage
    }

    val isLast = currentSlide == SLIDES.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
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
                            text = stringResource(Res.string.onboarding_skip),
                            color = MaterialTheme.appColors.textSecondary,
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

                OnboardingDots(
                    totalDots = SLIDES.size,
                    selectedIndex = currentSlide
                )

                Spacer(Modifier.height(18.dp))

                if (isLast) {
                    Button(
                        onClick = { viewModel.complete() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.appColors.cyanAccent
                        )
                    ) {
                        Text(
                            text = stringResource(Res.string.onboarding_start),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.appColors.navyDeep
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
                                    text = stringResource(Res.string.common_back_cd),
                                    color = MaterialTheme.appColors.textSecondary,
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
                                containerColor = MaterialTheme.appColors.cyanAccent
                            ),
                            modifier = Modifier.height(44.dp),
                            contentPadding = PaddingValues(horizontal = 28.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.onboarding_continue),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.appColors.navyDeep
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (slide.hideText) 1f else 0.55f),
            contentAlignment = Alignment.Center
        ) {
            when (slide.id) {
                "welcome"    -> SlideWelcome()
                "patrim"     -> SlidePatrimonio()
                "portfolio"  -> SlidePortfolio()
                "movim"      -> SlideMovimientos()
                "fiscal"     -> SlideFiscal()
                "realestate" -> OnboardingRealEstatePage(isVisible = true)
            }
        }

        if (!slide.hideText) {
            val slideTitle = when (slide.id) {
                "welcome" -> stringResource(Res.string.onboarding_welcome_title)
                "patrim" -> stringResource(Res.string.onboarding_slide_networth_title)
                "portfolio" -> stringResource(Res.string.onboarding_slide_portfolio_title)
                "movim" -> stringResource(Res.string.onboarding_slide_movements_title)
                "fiscal" -> stringResource(Res.string.onboarding_slide_fiscal_title)
                "realestate" -> stringResource(Res.string.onboarding_slide_realestate_title)
                else -> ""
            }
            val slideBody = when (slide.id) {
                "welcome" -> stringResource(Res.string.onboarding_welcome_subtitle)
                "patrim" -> stringResource(Res.string.onboarding_patrimonio_desc)
                "portfolio" -> stringResource(Res.string.onboarding_portfolio_desc)
                "movim" -> stringResource(Res.string.onboarding_movements_desc)
                "fiscal" -> stringResource(Res.string.onboarding_fiscal_desc)
                "realestate" -> stringResource(Res.string.onboarding_realestate_desc)
                else -> ""
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = slideTitle,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.appColors.textPrimary,
                    letterSpacing = (-1).sp,
                    lineHeight = 30.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = slideBody,
                    fontSize = 14.sp,
                    color = MaterialTheme.appColors.textSecondary,
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
                    .background(if (isSelected) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.navyBorder)
            )
        }
    }
}

// ─── Preview ───────────────────────────────────────────────

@Preview
@Composable
private fun OnboardingScreenPreview() {
    N3toTheme {
        OnboardingScreen(onComplete = {})
    }
}
