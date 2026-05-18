package es.aviferdev.n3to.ui.portfolio

import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.ui.theme.*
import es.aviferdev.n3to.ui.theme.formatAmountEuro

import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.portfolio_update_cancel
import n3to.composeapp.generated.resources.portfolio_update_no_price
import n3to.composeapp.generated.resources.portfolio_update_previous_price
import n3to.composeapp.generated.resources.portfolio_update_save
import n3to.composeapp.generated.resources.portfolio_update_save_hint
import n3to.composeapp.generated.resources.portfolio_update_price_title
import n3to.composeapp.generated.resources.portfolio_update_price_date
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/**
 * Sheet ligero para refrescar únicamente el precio actual de un activo
 * sin tener que abrir el formulario completo de edición. Persiste también
 * el timestamp de actualización (lo gestiona el ViewModel).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCurrentPriceSheet(
    asset: Asset,
    onConfirm: (newPrice: Double) -> Unit,
    onDismiss: () -> Unit
) {

    var price by remember(asset.id) {
        mutableStateOf(asset.currentPrice?.toString() ?: "")
    }
    val isValid = price.replace(',', '.').toDoubleOrNull()?.let { it >= 0 } == true

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderGray)
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
                text       = stringResource(Res.string.portfolio_update_price_title),
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = "${asset.ticker} · ${asset.name}",
                fontSize   = 13.sp,
                color      = TextSecondary,
                fontWeight = FontWeight.Medium,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))
            val currentLabel = if (asset.currentPrice != null) {
                stringResource(Res.string.portfolio_update_previous_price, formatAmountEuro(asset.currentPrice))
            } else {
                stringResource(Res.string.portfolio_update_no_price)
            }
            Text(
                text      = currentLabel,
                fontSize  = 12.sp,
                color     = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value         = price,
                onValueChange = { price = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                placeholder   = { Text("0,00", color = TextSecondary.copy(alpha = 0.6f), fontSize = 32.sp) },
                textStyle     = TextStyle(
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                    textAlign  = TextAlign.Center
                ),
                trailingIcon = {
                    Text(
                        "€",
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
                text     = stringResource(Res.string.portfolio_update_save_hint),
                fontSize = 11.sp,
                color    = TextSecondary
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    val value = price.replace(',', '.').toDoubleOrNull() ?: return@Button
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
                Text(stringResource(Res.string.portfolio_update_save), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.portfolio_update_cancel), fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}

@Preview
@Composable
private fun UpdateCurrentPriceSheetWithPricePreview() {
    N3toTheme {
        UpdateCurrentPriceSheet(
            asset = Asset(
                id = "1",
                accountId = "acc1",
                ticker = "AAPL",
                name = "Apple Inc.",
                notes = null,
                createdAt = nowMillis(),
                currentPrice = 178.50,
                currentPriceUpdatedAt = nowMillis(),
                archived = false
            ),
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun UpdateCurrentPriceSheetWithoutPricePreview() {
    N3toTheme {
        UpdateCurrentPriceSheet(
            asset = Asset(
                id = "2",
                accountId = "acc1",
                ticker = "GOOGL",
                name = "Alphabet Inc.",
                notes = null,
                createdAt = nowMillis(),
                currentPrice = null,
                currentPriceUpdatedAt = null,
                archived = false
            ),
            onConfirm = {},
            onDismiss = {}
        )
    }
}
