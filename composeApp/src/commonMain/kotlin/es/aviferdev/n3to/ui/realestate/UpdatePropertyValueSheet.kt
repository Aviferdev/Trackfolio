package es.aviferdev.n3to.ui.realestate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.ui.common.DeltaIndicator
import es.aviferdev.n3to.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePropertyValueSheet(
    property: RealEstateProperty,
    onDismiss: () -> Unit,
    onUpdate: (newValue: Double) -> Unit
) {
    var newValueText by remember { mutableStateOf(property.currentEstimatedValue.toString()) }

    val newValue = newValueText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val valueDiff = newValue - property.currentEstimatedValue
    val diffPercent = if (property.currentEstimatedValue > 0) {
        (valueDiff / property.currentEstimatedValue) * 100.0
    } else 0.0

    val isValid = newValue > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite,
        dragHandle = {
            Box(Modifier.padding(top = 12.dp, bottom = 4.dp).width(40.dp).height(4.dp)
                .clip(RoundedCornerShape(2.dp)).background(Color(0xFFBDBDBD)))
        }
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Text("Actualizar valor estimado", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(property.name, fontSize = 13.sp, color = TextTertiary)
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Valor actual", fontSize = 13.sp, color = TextSecondary)
                Text(formatAmountEuro(property.currentEstimatedValue), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = newValueText, onValueChange = { newValueText = it },
                label = { Text("Nuevo valor estimado (€)") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray, cursorColor = PrimaryDark, focusedLabelColor = PrimaryDark, unfocusedLabelColor = TextTertiary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                modifier = Modifier.fillMaxWidth()
            )

            if (newValue > 0 && newValue != property.currentEstimatedValue) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Diferencia", fontSize = 13.sp, color = TextSecondary)
                    DeltaIndicator(value = "${formatAmountEuro(valueDiff)} (${formatPercentSigned(diffPercent)})", isPositive = valueDiff >= 0)
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = { onUpdate(newValue) }, enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) { Text("Actualizar valor", fontWeight = FontWeight.Bold) }
        }
    }
}

@Preview
@Composable
private fun UpdatePropertyValueSheetPreview() {
    N3toTheme {
        UpdatePropertyValueSheet(
            property = RealEstateProperty(
                id = "1", accountId = "acc1", name = "Mi casa", address = "Calle Mayor 1, Madrid",
                propertyType = es.aviferdev.n3to.domain.model.PropertyType.PRIMARY_HOME,
                purchaseValue = 250000.0, currentEstimatedValue = 260000.0,
                acquisitionDate = 1672531200000, ownershipPercentage = 100.0,
                linkedLoanId = null, rentalStatus = es.aviferdev.n3to.domain.model.RentalStatus.OWN_USE,
                monthlyRent = null, mortgageReminderDismissed = false, archived = false
            ), onDismiss = {}, onUpdate = {}
        )
    }
}
