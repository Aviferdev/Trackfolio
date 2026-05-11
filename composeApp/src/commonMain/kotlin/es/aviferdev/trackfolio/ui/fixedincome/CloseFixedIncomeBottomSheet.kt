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

private fun formatEuro(value: Double): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 100).toInt()
    return "$intPart,${decPart.toString().padStart(2, '0')} €"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseFixedIncomeBottomSheet(
    position: FixedIncomePosition,
    onSave: (FixedIncomeCloseType, Long, FixedIncomeEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCloseType by remember { mutableStateOf(FixedIncomeCloseType.MATURITY) }
    var closeDateMillis by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    var grossAmountStr by remember { mutableStateOf(position.principal.toString()) }
    var irpfPercentStr by remember { mutableStateOf("19") }
    var commissionStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val availableCloseTypes = when (position.type) {
        FixedIncomeType.BOND, FixedIncomeType.BILL -> listOf(
            FixedIncomeCloseType.MATURITY,
            FixedIncomeCloseType.SECONDARY_SALE
        )
        FixedIncomeType.DEPOSIT -> listOf(
            FixedIncomeCloseType.MATURITY,
            FixedIncomeCloseType.EARLY_CANCELLATION
        )
    }

    val isValid = grossAmountStr.toDoubleOrNull() != null

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
                text = "Liquidar posición",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = position.name,
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(Modifier.height(20.dp))

            Text("Tipo de cierre", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))

            availableCloseTypes.forEach { closeType ->
                val label = when (closeType) {
                    FixedIncomeCloseType.MATURITY -> "Vencimiento"
                    FixedIncomeCloseType.SECONDARY_SALE -> "Venta en secundario"
                    FixedIncomeCloseType.EARLY_CANCELLATION -> "Cancelación anticipada"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCloseType = closeType }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedCloseType == closeType,
                        onClick = { selectedCloseType = closeType },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryDark)
                    )
                    Text(
                        text = label,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = grossAmountStr,
                onValueChange = { grossAmountStr = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Importe bruto recibido (€)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = irpfPercentStr,
                    onValueChange = { irpfPercentStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("IRPF (%)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
                )
                OutlinedTextField(
                    value = commissionStr,
                    onValueChange = { commissionStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Comisión (€)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            val gross = grossAmountStr.toDoubleOrNull() ?: 0.0
            val irpf = irpfPercentStr.toDoubleOrNull() ?: 0.0
            val commission = commissionStr.toDoubleOrNull() ?: 0.0
            val netAmount = gross - (gross * irpf / 100) - commission

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Importe neto", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            text = formatEuro(netAmount),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PositiveGreen
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val now = Clock.System.now().toEpochMilliseconds()
                    val eventId = "fie_${now}"
                    val eventType = when (selectedCloseType) {
                        FixedIncomeCloseType.MATURITY -> FixedIncomeEventType.MATURITY_SETTLEMENT
                        FixedIncomeCloseType.SECONDARY_SALE -> FixedIncomeEventType.SECONDARY_SALE
                        FixedIncomeCloseType.EARLY_CANCELLATION -> FixedIncomeEventType.EARLY_CANCELLATION
                    }
                    val event = FixedIncomeEvent(
                        id               = eventId,
                        positionId       = position.id,
                        type             = eventType,
                        grossAmount      = gross,
                        irpfPercent      = irpf,
                        commissionAmount = commission,
                        netAmount        = netAmount,
                        date             = closeDateMillis,
                        notes            = notes.ifBlank { null },
                        createdAt        = now
                    )
                    onSave(selectedCloseType, closeDateMillis, event)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar liquidación", fontSize = 15.sp, modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}