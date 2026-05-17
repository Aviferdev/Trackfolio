package es.aviferdev.n3to.ui.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.theme.*
import es.aviferdev.n3to.ui.theme.DragHandleColor
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
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
    val isEditing = loan != null

    var name by remember { mutableStateOf(loan?.name ?: "") }
    var selectedType by remember { mutableStateOf(loan?.type ?: LoanType.MORTGAGE) }
    var totalAmountText by remember { mutableStateOf(loan?.totalAmount?.toString() ?: "") }
    var interestRateText by remember { mutableStateOf(loan?.currentInterestRate?.toString() ?: "") }
    var totalInstallmentsText by remember { mutableStateOf(loan?.totalInstallments?.toString() ?: "") }
    var lenderName by remember { mutableStateOf(loan?.lenderName ?: "") }
    var notes by remember { mutableStateOf(loan?.notes ?: "") }
    var startDateMillis by remember { mutableStateOf(loan?.startDate ?: Clock.System.now().toEpochMilliseconds()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Calcular cuota previa
    val totalAmount = totalAmountText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val interestRate = interestRateText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val totalInstallments = totalInstallmentsText.toIntOrNull() ?: 0
    val previewPayment = if (totalAmount > 0 && totalInstallments > 0) {
        FrenchAmortizationCalculator.calculateMonthlyPayment(totalAmount, interestRate, totalInstallments)
    } else 0.0

    val isValid = name.isNotBlank() && totalAmount > 0 && totalInstallments > 0 && interestRate >= 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DragHandleColor)
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
                if (isEditing) "Editar préstamo" else "Nuevo préstamo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )

            Spacer(Modifier.height(20.dp))

            // ── Tipo de préstamo ─────────────────────────────────────────────
            Text("Tipo", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(LoanType.entries.toList()) { type ->
                    val selected = type == selectedType
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) PrimaryDark else SurfaceElevated)
                            .clickable { selectedType = type }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "${type.emoji} ${type.label}",
                            fontSize = 13.sp,
                            color = if (selected) Color.White else PrimaryDark
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Nombre ───────────────────────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del préstamo") },
                placeholder = { Text("Ej: Hipoteca piso Valencia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    focusedLabelColor = PrimaryDark
                )
            )

            Spacer(Modifier.height(12.dp))

            // ── Capital total ────────────────────────────────────────────────
            OutlinedTextField(
                value = totalAmountText,
                onValueChange = { totalAmountText = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                label = { Text("Capital total (€)") },
                placeholder = { Text("150000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    focusedLabelColor = PrimaryDark
                )
            )

            Spacer(Modifier.height(12.dp))

            // ── Tipo de interés + Plazo ──────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = interestRateText,
                    onValueChange = { interestRateText = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label = { Text("Interés anual (%)") },
                    placeholder = { Text("2,5") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDark,
                        focusedLabelColor = PrimaryDark
                    )
                )
                OutlinedTextField(
                    value = totalInstallmentsText,
                    onValueChange = { totalInstallmentsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Plazo (meses)") },
                    placeholder = { Text("360") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDark,
                        focusedLabelColor = PrimaryDark
                    )
                )
            }

            // ── Preview cuota ────────────────────────────────────────────────
            if (previewPayment > 0) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cuota mensual estimada", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            "${formatAmount(previewPayment)} €/mes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryDark
                        )
                    }
                }
            }

            // ── Fecha de inicio ───────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Text("Fecha de inicio", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(0.5.dp, BorderGray, RoundedCornerShape(10.dp))
                    .clickable { showStartDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(
                    text     = formatFullDate(startDateMillis),
                    fontSize = 14.sp,
                    color    = TextPrimary
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Entidad ──────────────────────────────────────────────────────
            OutlinedTextField(
                value = lenderName,
                onValueChange = { lenderName = it },
                label = { Text("Entidad (opcional)") },
                placeholder = { Text("CaixaBank") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    focusedLabelColor = PrimaryDark
                )
            )

            Spacer(Modifier.height(12.dp))

            // ── Notas ────────────────────────────────────────────────────────
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    focusedLabelColor = PrimaryDark
                )
            )

            Spacer(Modifier.height(24.dp))

            // ── Botón guardar ────────────────────────────────────────────────
            Button(
                onClick = {
                    if (!isValid || isLoading) return@Button
                    isLoading = true
                    val accountId = session.selectedAccountId.value ?: return@Button
                    val now = Clock.System.now().toEpochMilliseconds()
                    val endDate = startDateMillis + totalInstallments.toLong() * 30L * 24 * 60 * 60 * 1000

                    // Calcular la cuota mensual
                    val monthlyPayment = if (totalAmount > 0 && totalInstallments > 0 && interestRate >= 0) {
                        FrenchAmortizationCalculator.calculateMonthlyPayment(totalAmount, interestRate, totalInstallments)
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
                        loan!!.copy(
                            name                 = name.trim(),
                            type                 = selectedType,
                            currentInterestRate  = interestRate,
                            monthlyPayment       = monthlyPayment,
                            startDate            = startDateMillis,
                            endDate              = endDate,
                            lenderName           = lenderName.ifBlank { null },
                            notes                = notes.ifBlank { null }
                        )
                    } else {
                        Loan(
                            id                   = uuid4().toString(),
                            accountId            = accountId,
                            name                 = name.trim(),
                            type                 = selectedType,
                            totalAmount          = totalAmount,
                            outstandingPrincipal = outstandingPrincipal,
                            currentInterestRate  = interestRate,
                            monthlyPayment       = monthlyPayment,
                            totalInstallments    = totalInstallments,
                            paidInstallments     = paidInstallments,
                            startDate            = startDateMillis,
                            endDate              = endDate,
                            lenderName           = lenderName.ifBlank { null },
                            notes                = notes.ifBlank { null },
                            archived             = false,
                            createdAt            = now
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
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isEditing) "Guardar cambios" else "Guardar préstamo", fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ── Date picker para fecha de inicio ────────────────────────────────────────
    if (showStartDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = pickerState.selectedDateMillis
                    if (selected != null) {
                        startDateMillis = selected
                    }
                    showStartDatePicker = false
                }) { Text("Aceptar", color = PrimaryDark) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceWhite)
        ) {
            DatePicker(
                state = pickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = PrimaryDark,
                    todayDateBorderColor      = PrimaryDark
                )
            )
        }
    }
}

private fun formatFullDate(epochMillis: Long): String {
    val months = listOf("enero","febrero","marzo","abril","mayo","junio",
        "julio","agosto","septiembre","octubre","noviembre","diciembre")
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val ld: LocalDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
}
