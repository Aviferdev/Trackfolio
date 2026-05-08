package es.aviferdev.trackfolio.ui.fixedincome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.*
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFixedIncomeBottomSheet(
    platforms: List<es.aviferdev.trackfolio.domain.model.Platform>,
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
    var selectedFrequency by remember { mutableStateOf(InterestFrequency.AT_MATURITY) }
    var startDateMillis by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    var maturityDateMillis by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds() + 365L * 24 * 60 * 60 * 1000) }
    var selectedPlatformId by remember { mutableStateOf(platforms.firstOrNull()?.id ?: "") }
    var autoRenew by remember { mutableStateOf(false) }
    var feeNote by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val isValid = name.isNotBlank() &&
                  principalStr.toDoubleOrNull() != null &&
                  interestRateStr.toDoubleOrNull() != null &&
                  selectedPlatformId.isNotBlank()

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
                onValueChange = { interestRateStr = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("TAE (%)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            Spacer(Modifier.height(16.dp))

            Text("Frecuencia de interés", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Column {
                InterestFrequency.entries.forEach { freq ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFrequency = freq }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = selectedFrequency == freq,
                            onClick = { selectedFrequency = freq },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryDark)
                        )
                        Text(
                            text = freq.label,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            if (selectedType == FixedIncomeType.DEPOSIT) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = autoRenew,
                        onCheckedChange = { autoRenew = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryDark)
                    )
                    Text(
                        text = "Renovación automática al vencimiento",
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = selectedPlatformId,
                onValueChange = { },
                label = { Text("Plataforma / Broker") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Text("▼", fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            platforms.forEach { platform ->
                                DropdownMenuItem(
                                    text = { Text(platform.name) },
                                    onClick = {
                                        selectedPlatformId = platform.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                },
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
                    val rate = interestRateStr.toDoubleOrNull() ?: 0.0
                    val now = Clock.System.now().toEpochMilliseconds()
                    val positionId = "fi_${now}"
                    val eventId = "fie_${now}"

                    val position = FixedIncomePosition(
                        id                = positionId,
                        accountId         = accountId,
                        name              = name,
                        ticker            = ticker,
                        type              = selectedType,
                        notes             = notes.ifBlank { null },
                        principal         = principal,
                        quantity          = 1.0,
                        nominalPerUnit    = nominal,
                        interestRate      = rate,
                        interestFrequency = selectedFrequency,
                        startDate         = startDateMillis,
                        maturityDate      = maturityDateMillis,
                        platformId        = selectedPlatformId,
                        issuerId          = null,
                        autoRenew         = autoRenew,
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
}