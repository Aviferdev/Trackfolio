package es.aviferdev.n3to.ui.fixedincome

import es.aviferdev.n3to.platform.nowMillis
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
import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.domain.model.FixedIncomeEventType
import es.aviferdev.n3to.ui.theme.*

import org.jetbrains.compose.ui.tooling.preview.Preview

private fun formatEuro(value: Double): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 100).toInt()
    return "$intPart,${decPart.toString().padStart(2, '0')} €"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterCouponBottomSheet(
    positionName: String,
    onSave: (FixedIncomeEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var grossAmountStr by remember { mutableStateOf("") }
    var irpfPercentStr by remember { mutableStateOf("19") }
    var commissionStr by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(nowMillis()) }
    var notes by remember { mutableStateOf("") }

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
                text = "Registrar cupón / interés",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = positionName,
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = grossAmountStr,
                onValueChange = { grossAmountStr = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Importe bruto (€)") },
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
                    label = { Text("Retención (%)") },
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
                        Text("Importe neto estimado", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            text = formatEuro(netAmount),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryDark
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val now = nowMillis()
                    val eventId = "fie_${now}"
                    val event = FixedIncomeEvent(
                        id               = eventId,
                        positionId       = "",
                        type             = FixedIncomeEventType.COUPON,
                        grossAmount      = gross,
                        irpfPercent      = irpf,
                        commissionAmount = commission,
                        netAmount        = netAmount,
                        date             = dateMillis,
                        notes            = notes.ifBlank { null },
                        createdAt        = now
                    )
                    onSave(event)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Registrar cobro", fontSize = 15.sp, modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
private fun RegisterCouponBottomSheetPreview() {
    N3toTheme {
        RegisterCouponBottomSheet(
            positionName = "Bono Tesoro 2025",
            onSave = {},
            onDismiss = {}
        )
    }
}