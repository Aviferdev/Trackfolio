package es.aviferdev.n3to.ui.fiscal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import es.aviferdev.n3to.ui.common.button.ButtonLarge
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.fiscal.components.AnnualSummaryCard
import es.aviferdev.n3to.ui.fiscal.components.DebtsCard
import es.aviferdev.n3to.ui.fiscal.components.IncomeTaxBreakdownCard
import es.aviferdev.n3to.ui.fiscal.components.MonthlyBreakdownCard
import es.aviferdev.n3to.ui.fiscal.components.PdfPasswordSheet
import es.aviferdev.n3to.ui.fiscal.components.PortfolioCard
import es.aviferdev.n3to.ui.fiscal.components.TaxProfileBadge
import es.aviferdev.n3to.ui.fiscal.components.YearStepper
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.fiscal_generate_title
import n3to.composeapp.generated.resources.fiscal_generating_pdf
import n3to.composeapp.generated.resources.fiscal_net_only_warning
import n3to.composeapp.generated.resources.fiscal_no_data_year
import n3to.composeapp.generated.resources.fiscal_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// WRAPPER
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FiscalReportScreen(
    onBack: () -> Unit,
    viewModel: FiscalReportViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    state.successMessage?.let { LaunchedEffect(it) { viewModel.clearMessages() } }

    FiscalReportContent(
        state = state,
        onBack = onBack,
        onPreviousYear = { viewModel.previousYear() },
        onNextYear = { viewModel.nextYear() },
        onGeneratePdf = { viewModel.generatePdf() }
    )

    if (state.showPasswordSheet) {
        PdfPasswordSheet(
            onConfirm = { viewModel.confirmGeneratePdf(it) },
            onDismiss = { viewModel.cancelPasswordSheet() }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FiscalReportContent(
    state: FiscalReportUiState,
    onBack: () -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onGeneratePdf: () -> Unit,
    modifier: Modifier = Modifier,
    taxProfile: TaxProfileSnapshot? = state.activeTaxProfile
) {
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep)) {
        TopBarWithActionsApp(
            title = stringResource(Res.string.fiscal_title),
            subtitle = state.selectedYear,
            navigateBack = onBack,
            actions = {
                YearStepper(
                    year = state.selectedYear,
                    onPrevious = onPreviousYear,
                    onNext = onNextYear
                )
            }
        )

        if (state.hasNetOnlyIncomes && !state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.appColors.warnAmber.copy(alpha = 0.10f))
                    .border(
                        0.5.dp,
                        MaterialTheme.appColors.warnAmber.copy(alpha = 0.30f),
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.warnAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(Res.string.fiscal_net_only_warning),
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.warnAmber,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.appColors.cyanAccent)
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val report = state.reportData
                    if (report != null) {
                        if (taxProfile != null) TaxProfileBadge(taxProfile)
                        AnnualSummaryCard(report)
                        if (report.incomeTaxBreakdown.isNotEmpty()) IncomeTaxBreakdownCard(report)
                        MonthlyBreakdownCard(report)
                        if (report.activeDebts.isNotEmpty()) DebtsCard(report)
                        if (report.assetPositions.any { it.netQuantity > 0 || it.totalBought > 0 || it.totalSold > 0 }) {
                            PortfolioCard(report)
                        }
                    } else {
                        Box(
                            Modifier.fillMaxWidth().padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(44.dp),
                                    tint = MaterialTheme.appColors.cyanAccent.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    stringResource(
                                        Res.string.fiscal_no_data_year,
                                        state.selectedYear
                                    ),
                                    fontSize = 15.sp,
                                    color = MaterialTheme.appColors.textTertiary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(MaterialTheme.appColors.navySurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.appColors.navyBorder,
                            thickness = .5.dp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        state.errorMessage?.let { err ->
                            Text(
                                err,
                                fontSize = 11.sp,
                                color = MaterialTheme.appColors.expense,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        ButtonLarge(
                            text = Res.string.fiscal_generate_title,
                            textLoading = Res.string.fiscal_generating_pdf,
                            loading = state.isGenerating,
                            onClick = onGeneratePdf
                        )
                    }
                }
            }
        }
    }
}
