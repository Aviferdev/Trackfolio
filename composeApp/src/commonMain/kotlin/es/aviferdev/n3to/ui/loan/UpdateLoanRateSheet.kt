package es.aviferdev.n3to.ui.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.loan_apply_rate_change
import n3to.composeapp.generated.resources.loan_change_rate
import n3to.composeapp.generated.resources.loan_new_rate_label
import n3to.composeapp.generated.resources.loan_rate_placeholder
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateLoanRateSheet(
    currentRate: Double,
    onDismiss: () -> Unit,
    onConfirm: (newRate: Double, effectiveDate: Long) -> Unit
) {
    var newRateText by remember { mutableStateOf("") }
    val newRate = newRateText.replace(',', '.').toDoubleOrNull()
    val isValid = newRate != null && newRate >= 0 && newRate != currentRate

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.appColors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.dragHandle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                stringResource(Res.string.loan_change_rate),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.primary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Tipo actual: ${currentRate}%",
                fontSize = 14.sp,
                color = MaterialTheme.appColors.textSecondary
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = newRateText,
                onValueChange = {
                    newRateText = it.filter { c -> c.isDigit() || c == ',' || c == '.' }
                },
                label = { Text(stringResource(Res.string.loan_new_rate_label)) },
                placeholder = { Text(stringResource(Res.string.loan_rate_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.appColors.primary,
                    focusedLabelColor = MaterialTheme.appColors.primary
                )
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "La cuota se recalculará con el capital pendiente y las cuotas restantes.",
                fontSize = 12.sp,
                color = MaterialTheme.appColors.textSecondary
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(newRate, nowMillis())
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = isValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.appColors.primary)
            ) {
                Text(stringResource(Res.string.loan_apply_rate_change), fontSize = 16.sp)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Preview
@Composable
private fun UpdateLoanRateSheetPreview() {
    N3toTheme {
        UpdateLoanRateSheet(
            currentRate = 3.5,
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}
