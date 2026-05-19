package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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
import es.aviferdev.n3to.domain.usecase.asset.DetectPriceAnomalyUseCase
import es.aviferdev.n3to.ui.common.dialog.PriceAnomalyDialog
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
    onDismiss: () -> Unit,
    detectAnomaly: DetectPriceAnomalyUseCase? = null
) {

    var price by remember(asset.id) {
        mutableStateOf(asset.currentPrice?.toString() ?: "")
    }
    var showAnomalyDialog by remember { mutableStateOf(false) }
    var pendingPrice by remember { mutableStateOf(0.0) }
    var anomalyResult by remember { mutableStateOf<DetectPriceAnomalyUseCase.Result?>(null) }

    val isValid = price.replace(',', '.').toDoubleOrNull()?.let { it >= 0 } == true

    // ── Diálogo de anomalía ──────────────────────────────────────────────
    if (showAnomalyDialog && anomalyResult != null) {
        val result = anomalyResult as? DetectPriceAnomalyUseCase.Result.Suspicious
            ?: anomalyResult as? DetectPriceAnomalyUseCase.Result.Warning
        if (result != null) {
            PriceAnomalyDialog(
                previousPrice = when (result) {
                    is DetectPriceAnomalyUseCase.Result.Warning -> result.previousPrice
                    is DetectPriceAnomalyUseCase.Result.Suspicious -> result.previousPrice
                    else -> asset.currentPrice ?: 0.0
                },
                newPrice = pendingPrice,
                percentChange = when (result) {
                    is DetectPriceAnomalyUseCase.Result.Warning -> result.percentChange
                    is DetectPriceAnomalyUseCase.Result.Suspicious -> result.percentChange
                    else -> 0.0
                },
                likelyCause = when (result) {
                    is DetectPriceAnomalyUseCase.Result.Suspicious -> result.likelyCause
                    else -> null
                },
                isBlocking = result is DetectPriceAnomalyUseCase.Result.Suspicious,
                onCorrect = { correctedPrice ->
                    showAnomalyDialog = false
                    anomalyResult = null
                    onConfirm(correctedPrice)
                },
                onForceSave = {
                    showAnomalyDialog = false
                    anomalyResult = null
                    onConfirm(pendingPrice)
                },
                onDismiss = {
                    showAnomalyDialog = false
                    anomalyResult = null
                }
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.appColors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.border)
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
                color      = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = "${asset.ticker} · ${asset.name}",
                fontSize   = 13.sp,
                color      = MaterialTheme.appColors.textSecondary,
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
                color     = MaterialTheme.appColors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value         = price,
                onValueChange = { price = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                placeholder   = { Text("0,00", color = MaterialTheme.appColors.textSecondary.copy(alpha = 0.6f), fontSize = 32.sp) },
                textStyle     = TextStyle(
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.appColors.textPrimary,
                    textAlign  = TextAlign.Center
                ),
                trailingIcon = {
                    Text(
                        "€",
                        fontSize = 20.sp,
                        color    = MaterialTheme.appColors.textSecondary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.appColors.primary,
                    unfocusedBorderColor = MaterialTheme.appColors.border
                ),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text     = stringResource(Res.string.portfolio_update_save_hint),
                fontSize = 11.sp,
                color    = MaterialTheme.appColors.textSecondary
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    val value = price.replace(',', '.').toDoubleOrNull() ?: return@Button
                    // Verificar anomalía de precio antes de confirmar
                    if (detectAnomaly != null && asset.currentPrice != null) {
                        val result = detectAnomaly(asset.currentPrice, value, isManualEntry = true)
                        when (result) {
                            is DetectPriceAnomalyUseCase.Result.Suspicious,
                            is DetectPriceAnomalyUseCase.Result.Warning -> {
                                pendingPrice = value
                                anomalyResult = result
                                showAnomalyDialog = true
                            }
                            else -> onConfirm(value)
                        }
                    } else {
                        onConfirm(value)
                    }
                },
                enabled  = isValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = MaterialTheme.appColors.primary,
                    disabledContainerColor = MaterialTheme.appColors.primary.copy(alpha = 0.38f)
                )
            ) {
                Text(stringResource(Res.string.portfolio_update_save), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.portfolio_update_cancel), fontSize = 14.sp, color = MaterialTheme.appColors.textSecondary)
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
