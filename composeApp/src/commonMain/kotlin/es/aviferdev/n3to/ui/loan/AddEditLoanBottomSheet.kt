package es.aviferdev.n3to.ui.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.loan.FrenchAmortizationCalculator
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.LoanType
import es.aviferdev.n3to.domain.usecase.loan.SaveLoanUseCase
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.common.input.DatePickerRow
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_save_changes
import n3to.composeapp.generated.resources.fixedincome_start_date_label
import n3to.composeapp.generated.resources.loan_edit_title
import n3to.composeapp.generated.resources.loan_entity_label
import n3to.composeapp.generated.resources.loan_interest_label
import n3to.composeapp.generated.resources.loan_interest_placeholder
import n3to.composeapp.generated.resources.loan_lender_placeholder
import n3to.composeapp.generated.resources.loan_monthly_payment
import n3to.composeapp.generated.resources.loan_name_label
import n3to.composeapp.generated.resources.loan_name_placeholder
import n3to.composeapp.generated.resources.loan_new_title
import n3to.composeapp.generated.resources.loan_principal_label
import n3to.composeapp.generated.resources.loan_principal_placeholder
import n3to.composeapp.generated.resources.loan_save
import n3to.composeapp.generated.resources.loan_term_label
import n3to.composeapp.generated.resources.loan_term_placeholder
import n3to.composeapp.generated.resources.loan_type_label
import n3to.composeapp.generated.resources.portfolio_add_tx_notes_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLoanBottomSheet(
    loan: Loan? = null,
    onDismiss: () -> Unit,
    saveLoanUseCase: SaveLoanUseCase = koinInject(),
    updateLoanUseCase: es.aviferdev.n3to.domain.usecase.loan.UpdateLoanUseCase = koinInject(),
    session: AccountSession = koinInject()
) {
    val currency = LocalCurrencySymbol.current
    val isEditing = loan != null

    var name by remember { mutableStateOf(loan?.name ?: "") }
    var selectedType by remember { mutableStateOf(loan?.type ?: LoanType.MORTGAGE) }
    var totalAmountText by remember { mutableStateOf(loan?.totalAmount?.toString() ?: "") }
    var interestRateText by remember { mutableStateOf(loan?.currentInterestRate?.toString() ?: "") }
    var totalInstallmentsText by remember {
        mutableStateOf(
            loan?.totalInstallments?.toString() ?: ""
        )
    }
    var lenderName by remember { mutableStateOf(loan?.lenderName ?: "") }
    var notes by remember { mutableStateOf(loan?.notes ?: "") }
    var startDateMillis by remember { mutableStateOf(loan?.startDate ?: nowMillis()) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Calcular cuota previa
    val totalAmount = totalAmountText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val interestRate = interestRateText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val totalInstallments = totalInstallmentsText.toIntOrNull() ?: 0
    val previewPayment = if (totalAmount > 0 && totalInstallments > 0) {
        FrenchAmortizationCalculator.calculateMonthlyPayment(
            totalAmount,
            interestRate,
            totalInstallments
        )
    } else 0.0

    val isValid = name.isNotBlank() && totalAmount > 0 && totalInstallments > 0 && interestRate >= 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.appColors.navySurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.navyBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 32.dp)
        ) {
            Text(
                if (isEditing) stringResource(Res.string.loan_edit_title) else stringResource(Res.string.loan_new_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textPrimary
            )

            Spacer(Modifier.height(20.dp))

            // ── Tipo de préstamo ─────────────────────────────────────────────
            Text(
                stringResource(Res.string.loan_type_label),
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textSecondary
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(LoanType.entries.toList()) { type ->
                    val selected = type == selectedType
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.navySurfaceLight)
                            .clickable { selectedType = type }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "${type.emoji} ${type.label}",
                            fontSize = 13.sp,
                            color = if (selected) MaterialTheme.appColors.navyDeep else MaterialTheme.appColors.cyanAccent
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Nombre ───────────────────────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.loan_name_label)) },
                placeholder = { Text(stringResource(Res.string.loan_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors()
            )

            Spacer(Modifier.height(12.dp))

            // ── Capital total ────────────────────────────────────────────────
            OutlinedTextField(
                value = totalAmountText,
                onValueChange = {
                    totalAmountText = it.filter { c -> c.isDigit() || c == ',' || c == '.' }
                },
                label = { Text(stringResource(Res.string.loan_principal_label)) },
                placeholder = { Text(stringResource(Res.string.loan_principal_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = fieldColors()
            )

            Spacer(Modifier.height(12.dp))

            // ── Tipo de interés + Plazo ──────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = interestRateText,
                    onValueChange = {
                        interestRateText = it.filter { c -> c.isDigit() || c == ',' || c == '.' }
                    },
                    label = { Text(stringResource(Res.string.loan_interest_label)) },
                    placeholder = { Text(stringResource(Res.string.loan_interest_placeholder)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    value = totalInstallmentsText,
                    onValueChange = { totalInstallmentsText = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(Res.string.loan_term_label)) },
                    placeholder = { Text(stringResource(Res.string.loan_term_placeholder)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors()
                )
            }

            // ── Preview cuota ────────────────────────────────────────────────
            if (previewPayment > 0) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(Res.string.loan_monthly_payment),
                            fontSize = 13.sp,
                            color = MaterialTheme.appColors.textSecondary
                        )
                        Text(
                            "${formatAmount(previewPayment)} $currency/mes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.appColors.cyanAccent
                        )
                    }
                }
            }

            // ── Fecha de inicio ───────────────────────────────────────────────
            Spacer(Modifier.height(12.dp))
            DatePickerRow(
                label = stringResource(Res.string.fixedincome_start_date_label),
                dateMillis = startDateMillis,
                onDateSelected = { startDateMillis = it }
            )

            Spacer(Modifier.height(12.dp))

            // ── Entidad ──────────────────────────────────────────────────────
            OutlinedTextField(
                value = lenderName,
                onValueChange = { lenderName = it },
                label = { Text(stringResource(Res.string.loan_entity_label)) },
                placeholder = { Text(stringResource(Res.string.loan_lender_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors()
            )

            Spacer(Modifier.height(12.dp))

            // ── Notas ────────────────────────────────────────────────────────
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(Res.string.portfolio_add_tx_notes_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = fieldColors()
            )

            Spacer(Modifier.height(24.dp))

            // ── Botón guardar ────────────────────────────────────────────────
            Button(
                onClick = {
                    if (!isValid || isLoading) return@Button
                    isLoading = true
                    val accountId = session.selectedAccountId.value ?: return@Button
                    val now = nowMillis()
                    val endDate =
                        startDateMillis + totalInstallments.toLong() * 30L * 24 * 60 * 60 * 1000

                    // Calcular la cuota mensual
                    val monthlyPayment =
                        if (totalAmount > 0 && totalInstallments > 0 && interestRate >= 0) {
                            FrenchAmortizationCalculator.calculateMonthlyPayment(
                                totalAmount,
                                interestRate,
                                totalInstallments
                            )
                        } else 0.0

                    // Calcular cuotas pagadas basándose en la fecha de inicio
                    val monthsSinceStart = if (startDateMillis < now) {
                        val diffMillis = now - startDateMillis
                        val diffDays = diffMillis / (24L * 60 * 60 * 1000)
                        (diffDays / 30).toInt()
                    } else 0

                    val paidInstallments = monthsSinceStart.coerceIn(0, totalInstallments)

                    // Calcular capital pendiente usando el cuadro de amortización
                    val outstandingPrincipal = if (paidInstallments > 0 && monthlyPayment > 0) {
                        val schedule = FrenchAmortizationCalculator.generateSchedule(
                            totalAmount, interestRate, totalInstallments, startDateMillis
                        )
                        // Obtener el capital pendiente después de las cuotas pagadas
                        if (paidInstallments < schedule.size) {
                            schedule[paidInstallments].outstandingBalance
                        } else {
                            0.0
                        }
                    } else {
                        totalAmount
                    }

                    val loan = if (isEditing) {
                        // Para edición, mantener los valores originales de cuotas pagadas y capital pendiente
                        // solo actualizar los datos editables
                        loan.copy(
                            name = name.trim(),
                            type = selectedType,
                            currentInterestRate = interestRate,
                            monthlyPayment = monthlyPayment,
                            startDate = startDateMillis,
                            endDate = endDate,
                            lenderName = lenderName.ifBlank { null },
                            notes = notes.ifBlank { null }
                        )
                    } else {
                        Loan(
                            id = uuid4().toString(),
                            accountId = accountId,
                            name = name.trim(),
                            type = selectedType,
                            totalAmount = totalAmount,
                            outstandingPrincipal = outstandingPrincipal,
                            currentInterestRate = interestRate,
                            monthlyPayment = monthlyPayment,
                            totalInstallments = totalInstallments,
                            paidInstallments = paidInstallments,
                            startDate = startDateMillis,
                            endDate = endDate,
                            lenderName = lenderName.ifBlank { null },
                            notes = notes.ifBlank { null },
                            archived = false,
                            createdAt = now
                        )
                    }
                    scope.launch {
                        if (isEditing) {
                            updateLoanUseCase(loan)
                        } else {
                            saveLoanUseCase(loan)
                        }
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = isValid && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.appColors.cyanAccent,
                    contentColor = MaterialTheme.appColors.navyDeep
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (isEditing) stringResource(Res.string.common_save_changes) else stringResource(
                            Res.string.loan_save
                        ), fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.appColors.cyanAccent,
    unfocusedBorderColor = MaterialTheme.appColors.navyBorder,
    cursorColor = MaterialTheme.appColors.cyanAccent,
    focusedLabelColor = MaterialTheme.appColors.cyanAccent,
    unfocusedLabelColor = MaterialTheme.appColors.textSecondary,
    focusedTextColor = MaterialTheme.appColors.textPrimary,
    unfocusedTextColor = MaterialTheme.appColors.textPrimary,
    focusedContainerColor = MaterialTheme.appColors.navySurfaceLight,
    unfocusedContainerColor = MaterialTheme.appColors.navySurfaceLight
)


