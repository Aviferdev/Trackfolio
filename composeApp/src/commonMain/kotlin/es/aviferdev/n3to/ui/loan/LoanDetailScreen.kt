package es.aviferdev.n3to.ui.loan

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.loan.components.AmortizationTableSection
import es.aviferdev.n3to.ui.loan.components.LoanHeaderCard
import es.aviferdev.n3to.ui.loan.components.LoanRateHistorySection
import es.aviferdev.n3to.ui.loan.components.LoanSummaryCard
import es.aviferdev.n3to.ui.loan.components.SectionLabel
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.fixedincome_interest_label
import n3to.composeapp.generated.resources.loan_archive_confirm_message
import n3to.composeapp.generated.resources.loan_archive_title
import n3to.composeapp.generated.resources.loan_change_rate
import n3to.composeapp.generated.resources.loan_detail_title
import n3to.composeapp.generated.resources.loan_monthly_payment
import n3to.composeapp.generated.resources.loan_not_found
import n3to.composeapp.generated.resources.loan_pending_capital
import n3to.composeapp.generated.resources.loan_progress
import n3to.composeapp.generated.resources.loan_term_label
import n3to.composeapp.generated.resources.settings_edit_cd
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// ═══════════════════════════════════════════════════════════════════════════════
// WRAPPER
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    loanId: String,
    onBack: () -> Unit,
    viewModel: LoanDetailViewModel = koinViewModel(parameters = { parametersOf(loanId) })
) {
    val uiState by viewModel.uiState.collectAsState()
    var showArchiveConfirm by remember { mutableStateOf(false) }

    LoanDetailContent(
        uiState = uiState,
        onBack = onBack,
        onEditClick = { viewModel.openEditSheet() },
        onRateChangeClick = { viewModel.openRateSheet() },
        onArchiveClick = { showArchiveConfirm = true }
    )

    if (showArchiveConfirm && uiState.loan != null) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirm = false },
            containerColor = MaterialTheme.appColors.surface,
            icon = { Text("⚠️", fontSize = 26.sp) },
            title = {
                Text(
                    stringResource(Res.string.loan_archive_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    stringResource(Res.string.loan_archive_confirm_message, uiState.loan!!.name),
                    fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.archive(); showArchiveConfirm = false; onBack()
                }) {
                    Text(
                        stringResource(Res.string.loan_archive_title),
                        color = MaterialTheme.appColors.expense,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveConfirm = false }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.primary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (uiState.showEditSheet && uiState.loan != null) {
        AddEditLoanBottomSheet(loan = uiState.loan, onDismiss = { viewModel.closeEditSheet() })
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailContent(
    uiState: LoanDetailUiState,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onRateChangeClick: () -> Unit,
    onArchiveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.appColors.background)
    ) {
        TopBarWithActionsApp(
            title = uiState.loan?.name ?: stringResource(Res.string.loan_detail_title),
            navigateBack = onBack,
            actions = {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Outlined.Edit,
                        stringResource(Res.string.settings_edit_cd),
                        tint = MaterialTheme.appColors.textSecondary
                    )
                    Icon(
                        Icons.Outlined.Edit,
                        stringResource(Res.string.loan_change_rate),
                        tint = MaterialTheme.appColors.textSecondary
                    )
                    Icon(
                        Icons.Outlined.Delete,
                        stringResource(Res.string.loan_archive_title),
                        tint = MaterialTheme.appColors.expense
                    )
                }
            }
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.appColors.primary)
            }
        } else if (uiState.loan == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(Res.string.loan_not_found),
                    color = MaterialTheme.appColors.textTertiary,
                    fontSize = 13.sp
                )
            }
        } else {
            val loan = uiState.loan

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { LoanHeaderCard(loan = loan) }
                item { LoanSummaryCard(loan = loan) }

                if (uiState.rateChanges.isNotEmpty()) {
                    item { SectionLabel("Historial de tipo de interés") }
                    items(uiState.rateChanges, key = { it.id }) { change ->
                        LoanRateHistorySection(rateChanges = listOf(change))
                    }
                }

                item { SectionLabel("Cuadro de amortización") }
                item {
                    AmortizationTableSection(
                        schedule = uiState.schedule,
                        paidInstallments = loan.paidInstallments
                    )
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREVIEW
// ═══════════════════════════════════════════════════════════════════════════════

@Preview
@Composable
fun LoanDetailContentPreview() {
    N3toTheme {
        LoanDetailContent(
            uiState = LoanDetailUiState(
                isLoading = false,
                loan = Loan(
                    id = "preview-1",
                    accountId = "acct-1",
                    name = "Préstamo Hipoteca",
                    type = es.aviferdev.n3to.domain.model.LoanType.MORTGAGE,
                    totalAmount = 120000.0,
                    outstandingPrincipal = 98000.0,
                    monthlyPayment = 850.0,
                    currentInterestRate = 2.75,
                    totalInstallments = 240,
                    paidInstallments = 36,
                    startDate = 1700000000000,
                    endDate = 1700000000000 + 240L * 30L * 24L * 3600L * 1000L,
                    lenderName = "Banco Ejemplo",
                    notes = null,
                    archived = false,
                    createdAt = 1700000000000
                ),
                rateChanges = emptyList(),
                schedule = emptyList()
            ),
            onBack = {},
            onEditClick = {},
            onRateChangeClick = {},
            onArchiveClick = {}
        )
    }
}

// ── Secciones extraídas a loan/components/ ────────────────────────────────────
