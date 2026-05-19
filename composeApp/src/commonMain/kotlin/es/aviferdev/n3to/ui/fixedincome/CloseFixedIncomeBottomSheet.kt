package es.aviferdev.n3to.ui.fixedincome

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
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
import es.aviferdev.n3to.domain.model.*
import es.aviferdev.n3to.ui.theme.*

import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.fixedincome_close_confirm_btn
import n3to.composeapp.generated.resources.fixedincome_close_early_type
import n3to.composeapp.generated.resources.fixedincome_close_maturity_type
import n3to.composeapp.generated.resources.fixedincome_close_secondary_type
import n3to.composeapp.generated.resources.fixedincome_close_title
import n3to.composeapp.generated.resources.fixedincome_close_type_label
import n3to.composeapp.generated.resources.fixedincome_commission_eur_label
import n3to.composeapp.generated.resources.fixedincome_gross_received_label
import n3to.composeapp.generated.resources.fixedincome_irpf_label
import n3to.composeapp.generated.resources.fixedincome_net_amount_label
import n3to.composeapp.generated.resources.portfolio_add_tx_notes_label
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

private fun formatEuro(value: Double): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 100).toInt()
    return "$intPart,${decPart.toString().padStart(2, '0')} €"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseFixedIncomeBottomSheet(
    position: FixedIncomePosition,
    preselectedCloseType: FixedIncomeCloseType? = null,
    onSave: (FixedIncomeCloseType, Long, FixedIncomeEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCloseType by remember { mutableStateOf(preselectedCloseType ?: FixedIncomeCloseType.MATURITY) }
    var closeDateMillis by remember { mutableStateOf(nowMillis()) }
    var grossAmountStr by remember { mutableStateOf(position.principal.toString()) }
    var irpfPercentStr by remember { mutableStateOf("19") }
    var commissionStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Los tipos de cierre disponibles dependen del tipo de instrumento
    val availableCloseTypes = when (position.type) {
        FixedIncomeType.DEPOSIT -> listOf(
            FixedIncomeCloseType.MATURITY,
            FixedIncomeCloseType.EARLY_CANCELLATION
        )
        // BILL, BOND, GOVERNMENT_OBLIGATION, CORPORATE_BOND -> venta en secundario
        else -> listOf(
            FixedIncomeCloseType.MATURITY,
            FixedIncomeCloseType.SECONDARY_SALE
        )
    }

    // Si el tipo preseleccionado no está disponible, usar el primero disponible
    LaunchedEffect(preselectedCloseType, availableCloseTypes) {
        if (preselectedCloseType != null && preselectedCloseType !in availableCloseTypes) {
            selectedCloseType = availableCloseTypes.first()
        }
    }

    val isValid = grossAmountStr.toDoubleOrNull() != null

    val navyFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.appColors.cyanAccent,
        unfocusedBorderColor = MaterialTheme.appColors.navyBorder,
        focusedLabelColor = MaterialTheme.appColors.cyanAccent,
        unfocusedLabelColor = MaterialTheme.appColors.textSecondary,
        cursorColor = MaterialTheme.appColors.cyanAccent,
        focusedTextColor = MaterialTheme.appColors.textPrimary,
        unfocusedTextColor = MaterialTheme.appColors.textPrimary
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.appColors.navySurface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.fixedincome_close_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = position.name,
                fontSize = 14.sp,
                color = MaterialTheme.appColors.cyanAccent.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(20.dp))

            Text(stringResource(Res.string.fixedincome_close_type_label), fontSize = 12.sp, color = MaterialTheme.appColors.textSecondary)
            Spacer(Modifier.height(8.dp))

            availableCloseTypes.forEach { closeType ->
                val label = when (closeType) {
                    FixedIncomeCloseType.MATURITY -> stringResource(Res.string.fixedincome_close_maturity_type)
                    FixedIncomeCloseType.SECONDARY_SALE -> stringResource(Res.string.fixedincome_close_secondary_type)
                    FixedIncomeCloseType.EARLY_CANCELLATION -> stringResource(Res.string.fixedincome_close_early_type)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCloseType = closeType }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedCloseType == closeType,
                        onClick = { selectedCloseType = closeType },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.appColors.cyanAccent,
                            unselectedColor = MaterialTheme.appColors.navyBorder
                        )
                    )
                    Text(
                        text = label,
                        fontSize = 15.sp,
                        color = if (selectedCloseType == closeType) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textSecondary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = grossAmountStr,
                onValueChange = { grossAmountStr = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text(stringResource(Res.string.fixedincome_gross_received_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = navyFieldColors
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = irpfPercentStr,
                    onValueChange = { irpfPercentStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(stringResource(Res.string.fixedincome_irpf_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = navyFieldColors
                )
                OutlinedTextField(
                    value = commissionStr,
                    onValueChange = { commissionStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(stringResource(Res.string.fixedincome_commission_eur_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = navyFieldColors
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(Res.string.portfolio_add_tx_notes_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = navyFieldColors
            )

            val gross = grossAmountStr.toDoubleOrNull() ?: 0.0
            val irpf = irpfPercentStr.toDoubleOrNull() ?: 0.0
            val commission = commissionStr.toDoubleOrNull() ?: 0.0
            val netAmount = gross - (gross * irpf / 100) - commission

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurfaceLight),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(Res.string.fixedincome_net_amount_label), fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary)
                    Text(
                        text = formatEuro(netAmount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.pnlPositive
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val now = nowMillis()
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.appColors.cyanAccent,
                    contentColor = MaterialTheme.appColors.navyDeep
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(Res.string.fixedincome_close_confirm_btn), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun createMockPosition(): FixedIncomePosition {
    val now = nowMillis()
    val dayInMillis = 24 * 60 * 60 * 1000L
    return FixedIncomePosition(
        id = "1",
        accountId = "acc1",
        name = "Bono Tesoro 2025",
        ticker = "ES0000000001",
        type = FixedIncomeType.BOND,
        notes = null,
        principal = 10000.0,
        quantity = 10.0,
        nominalPerUnit = 1000.0,
        interestRate = 3.5,
        interestFrequency = InterestFrequency.ANNUAL,
        startDate = now - (365 * dayInMillis),
        maturityDate = now + (30 * dayInMillis),
        platformId = "platform1",
        issuerId = null,
        autoRenew = false,
        archived = false,
        closedAt = null,
        closeType = null,
        feeNote = null,
        createdAt = now - (365 * dayInMillis)
    )
}

@Preview
@Composable
private fun CloseFixedIncomeBottomSheetPreview() {
    N3toTheme {
        CloseFixedIncomeBottomSheet(
            position = createMockPosition(),
            onSave = { _, _, _ -> },
            onDismiss = {}
        )
    }
}