package es.aviferdev.trackfolio.ui.home

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Sheet obligatorio para configurar el saldo inicial de una cuenta.
 * No tiene botón de cancelar/omitir: la cuenta no es usable sin saldo inicial.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetInitialBalanceBottomSheet(
    accountName: String = "",
    currency: String = "€",
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val isValid = amount.isNotBlank() &&
        amount.replace(',', '.').toDoubleOrNull()?.let { it >= 0 } == true

    ModalBottomSheet(
        onDismissRequest = { /* Bloqueado — solo se puede cerrar confirmando */ },
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFBDBDBD))
            )
        }
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text       = "Saldo inicial",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )

            if (accountName.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = accountName,
                    fontSize = 14.sp,
                    color    = PrimaryDark,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text      = "Introduce el saldo actual de esta cuenta.\nEsto es obligatorio antes de poder registrar movimientos.",
                fontSize  = 13.sp,
                color     = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value         = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                placeholder   = { Text("0,00", color = TextSecondary.copy(alpha = 0.6f), fontSize = 32.sp) },
                textStyle     = TextStyle(
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                    textAlign  = TextAlign.Center
                ),
                trailingIcon = {
                    Text(
                        currency,
                        fontSize = 20.sp,
                        color    = TextSecondary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                ),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text     = "Puede ser 0 si la cuenta está vacía.",
                fontSize = 11.sp,
                color    = TextSecondary
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    val value = amount.replace(',', '.').toDoubleOrNull() ?: return@Button
                    onConfirm(value)
                },
                enabled  = isValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = PrimaryDark,
                    disabledContainerColor = PrimaryDark.copy(alpha = 0.38f)
                )
            ) {
                Text("Establecer saldo", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Preview
@Composable
private fun SetInitialBalanceBottomSheetPreview() {
    TrackfolioTheme {
        SetInitialBalanceBottomSheet(
            accountName = "Cuenta Principal",
            currency = "€",
            onConfirm = {}
        )
    }
}
