package es.aviferdev.trackfolio.ui.fixedincome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import es.aviferdev.trackfolio.domain.model.*
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFixedIncomeBottomSheet(
    platforms: List<es.aviferdev.trackfolio.domain.model.Platform>,
    categories: List<es.aviferdev.trackfolio.domain.model.AssetCategory>,
    accountId: String,
    onSave: (FixedIncomePosition, FixedIncomeEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ticker by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FixedIncomeType.DEPOSIT) }
    var principalStr by remember { mutableStateOf("") }
    var nominalPerUnitStr by remember { mutableStateOf("") }
    var interestRateStr by remember { mutableStateOf("") }
    var startDateMillis by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    var maturityDateMillis by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds() + 365L * 24 * 60 * 60 * 1000) }
    var entityName by remember { mutableStateOf("") }
    var feeNote by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    fun parseInterestRate(input: String): Double? {
        val normalized = input.replace(",", ".")
        return normalized.toDoubleOrNull()
    }

    val isValid = name.isNotBlank() &&
            principalStr.toDoubleOrNull() != null &&
            parseInterestRate(interestRateStr) != null &&
            entityName.isNotBlank()

    val entityLabel = when (selectedType) {
        FixedIncomeType.DEPOSIT -> "Entidad financiera"
        FixedIncomeType.BOND -> "Emisor (Estado/Empresa)"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Nueva posición de renta fija",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre (ej: Depósito Sabadell 12M)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = ticker,
                onValueChange = { ticker = it },
                label = { Text("Ticker / ISIN (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            Spacer(Modifier.height(16.dp))

            Text("Tipo de producto", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FixedIncomeType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text("${type.emoji} ${type.label}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryDark,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = principalStr,
                    onValueChange = { principalStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Capital (€)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
                )
                OutlinedTextField(
                    value = nominalPerUnitStr.ifBlank { principalStr },
                    onValueChange = { nominalPerUnitStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Nominal (€)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = interestRateStr,
                onValueChange = { interestRateStr = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                label = { Text("TAE (%)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = entityName,
                onValueChange = { entityName = it },
                label = { Text(entityLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = formatDate(maturityDateMillis),
                onValueChange = { },
                label = { Text("Fecha de vencimiento") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = feeNote,
                onValueChange = { feeNote = it },
                label = { Text("Notas de comisiones (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val principal = principalStr.toDoubleOrNull() ?: 0.0
                    val nominal = nominalPerUnitStr.toDoubleOrNull() ?: principal
                    val rate = parseInterestRate(interestRateStr) ?: 0.0
                    val now = Clock.System.now().toEpochMilliseconds()
                    val positionId = "fi_${now}"
                    val eventId = "fie_${now}"

                    val position = FixedIncomePosition(
                        id                = positionId,
                        accountId         = accountId,
                        assetCategoryId   = null,
                        name              = name,
                        ticker            = ticker,
                        type              = selectedType,
                        notes             = notes.ifBlank { null },
                        principal         = principal,
                        quantity          = 1.0,
                        nominalPerUnit    = nominal,
                        interestRate      = rate,
                        interestFrequency = InterestFrequency.AT_MATURITY,
                        startDate         = startDateMillis,
                        maturityDate      = maturityDateMillis,
                        platformId        = "",
                        issuerId          = entityName.ifBlank { null },
                        autoRenew         = false,
                        archived          = false,
                        closedAt          = null,
                        closeType         = null,
                        feeNote           = feeNote.ifBlank { null },
                        createdAt         = now
                    )

                    val acquisitionEvent = FixedIncomeEvent(
                        id               = eventId,
                        positionId       = positionId,
                        type             = FixedIncomeEventType.ACQUISITION,
                        grossAmount      = principal,
                        irpfPercent      = 0.0,
                        commissionAmount = 0.0,
                        netAmount        = -principal,
                        date             = startDateMillis,
                        notes            = "Adquisición de $name",
                        createdAt        = now
                    )

                    onSave(position, acquisitionEvent)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Crear posición", fontSize = 15.sp, modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = maturityDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { maturityDateMillis = it }
                    showDatePicker = false
                }) {
                    Text("Aceptar", color = PrimaryDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}