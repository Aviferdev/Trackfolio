package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.ui.theme.*

import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.fixedincome_irpf_label
import n3to.composeapp.generated.resources.portfolio_add_tx_select_asset
import n3to.composeapp.generated.resources.portfolio_dividend_asset_label
import n3to.composeapp.generated.resources.portfolio_dividend_gross_hint
import n3to.composeapp.generated.resources.portfolio_dividend_gross_label
import n3to.composeapp.generated.resources.portfolio_dividend_gross_short
import n3to.composeapp.generated.resources.portfolio_dividend_net_short
import n3to.composeapp.generated.resources.portfolio_dividend_register
import n3to.composeapp.generated.resources.portfolio_dividend_select_asset_error
import n3to.composeapp.generated.resources.portfolio_dividend_title
import n3to.composeapp.generated.resources.portfolio_dividend_title_alt
import n3to.composeapp.generated.resources.portfolio_dividend_valid_amount_error
import n3to.composeapp.generated.resources.portfolio_dividend_withholding_label
import n3to.composeapp.generated.resources.portfolio_dividend_withholding_short
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Sheet para registrar un dividendo sobre un activo.
 *
 * @param fixedAssetName si se proporciona, el activo ya est\u00e1 seleccionado (desde AssetHistoryScreen)
 * @param allAssets si se proporciona, se muestra un selector de activos (desde PortfolioScreen)
 * @param onSave callback con (assetId?, grossAmount, irpfPercent, date).
 *        assetId es null cuando fixedAssetName != null (el caller ya sabe el activo)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDividendBottomSheet(
    fixedAssetName: String? = null,
    allAssets: List<Asset> = emptyList(),
    onSave: (assetId: String?, grossAmount: Double, irpfPercent: Double, date: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val currency = LocalCurrencySymbol.current
    val showAssetSelector = fixedAssetName == null && allAssets.isNotEmpty()

    var selectedAssetId by remember { mutableStateOf(allAssets.firstOrNull()?.id) }
    var grossAmountText by remember { mutableStateOf("") }
    var irpfPercentText by remember { mutableStateOf("19") }
    var error by remember { mutableStateOf<String?>(null) }

    val grossAmount = grossAmountText.replace(',', '.').toDoubleOrNull()
    val irpfPercent = irpfPercentText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val netAmount = if (grossAmount != null && grossAmount > 0) {
        grossAmount - (grossAmount * irpfPercent / 100.0)
    } else null

    val displayName = fixedAssetName ?: allAssets.find { it.id == selectedAssetId }?.name ?: ""

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
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.portfolio_dividend_title_alt),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.textPrimary
            )
            if (displayName.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = displayName,
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            // \u2500\u2500 Selector de activo (solo desde PortfolioScreen) \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500
            if (showAssetSelector) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(Res.string.portfolio_dividend_asset_label),
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.textSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allAssets) { asset ->
                            val isSel = asset.id == selectedAssetId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSel) MaterialTheme.appColors.primary.copy(
                                            alpha = 0.1f
                                        ) else Color.Transparent
                                    )
                                    .border(
                                        if (isSel) 1.5.dp else 0.5.dp,
                                        if (isSel) MaterialTheme.appColors.primary else MaterialTheme.appColors.border,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedAssetId = asset.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = asset.ticker,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.appColors.primary else MaterialTheme.appColors.textSecondary
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // \u2500\u2500 Neto calculado \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500
            if (netAmount != null) {
                Text(
                    text = "+ ${formatAmount(netAmount)} $currency",
                    fontSize = 28.sp,
                    color = MaterialTheme.appColors.income,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = stringResource(Res.string.portfolio_dividend_gross_hint),
                    fontSize = 14.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.appColors.border, thickness = 0.5.dp)
            Spacer(Modifier.height(16.dp))

            // \u2500\u2500 Importe bruto \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(Res.string.portfolio_dividend_gross_label),
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = grossAmountText,
                    onValueChange = {
                        grossAmountText =
                            it.filter { c -> c.isDigit() || c == ',' || c == '.' }; error = null
                    },
                    placeholder = {
                        Text(
                            "0,00",
                            color = MaterialTheme.appColors.textSecondary.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    },
                    suffix = {
                        Text(
                            currency,
                            color = MaterialTheme.appColors.textSecondary,
                            fontSize = 14.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.appColors.primary,
                        unfocusedBorderColor = MaterialTheme.appColors.border
                    ),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.appColors.textPrimary
                    )
                )
            }

            Spacer(Modifier.height(14.dp))

            // \u2500\u2500 Retenci\u00f3n IRPF \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(Res.string.portfolio_dividend_withholding_label),
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = irpfPercentText,
                    onValueChange = {
                        irpfPercentText = it.filter { c -> c.isDigit() || c == ',' || c == '.' }
                    },
                    placeholder = {
                        Text(
                            "19",
                            color = MaterialTheme.appColors.textSecondary.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    },
                    suffix = {
                        Text(
                            "%",
                            color = MaterialTheme.appColors.textSecondary,
                            fontSize = 14.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.appColors.primary,
                        unfocusedBorderColor = MaterialTheme.appColors.border
                    ),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.appColors.textPrimary
                    )
                )
            }

            // \u2500\u2500 Resumen \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500
            if (grossAmount != null && grossAmount > 0) {
                Spacer(Modifier.height(14.dp))
                val irpfAmount = grossAmount * irpfPercent / 100.0
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.appColors.income.copy(alpha = 0.07f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryItem(
                            stringResource(Res.string.portfolio_dividend_gross_short),
                            grossAmount,
                            MaterialTheme.appColors.textPrimary
                        )
                        if (irpfAmount > 0) SummaryItem(
                            stringResource(Res.string.portfolio_dividend_withholding_short),
                            irpfAmount,
                            MaterialTheme.appColors.expense
                        )
                        SummaryItem(
                            stringResource(Res.string.portfolio_dividend_net_short),
                            netAmount ?: 0.0,
                            MaterialTheme.appColors.income
                        )
                    }
                }
            }

            // \u2500\u2500 Error \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.appColors.expense, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            // \u2500\u2500 Bot\u00f3n guardar \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500
            val selectAssetError = stringResource(Res.string.portfolio_dividend_select_asset_error)
            val validAmountError = stringResource(Res.string.portfolio_dividend_valid_amount_error)
            Button(
                onClick = {
                    if (showAssetSelector && selectedAssetId == null) {
                        error = selectAssetError
                        return@Button
                    }
                    val ga = grossAmountText.replace(',', '.').toDoubleOrNull()
                    if (ga == null || ga <= 0) {
                        error = validAmountError
                        return@Button
                    }
                    val pct = irpfPercentText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val now = nowMillis()
                    onSave(selectedAssetId, ga, pct, now)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.appColors.primary,
                    disabledContainerColor = MaterialTheme.appColors.primary.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    stringResource(Res.string.portfolio_dividend_register),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: Double, color: Color) {
    val currency = LocalCurrencySymbol.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.appColors.textSecondary)
        Text(
            text = "${formatAmount(value)} $currency",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

private fun createMockAssets(): List<Asset> {
    val now = nowMillis()
    return listOf(
        Asset(
            id = "1",
            accountId = "acc1",
            ticker = "AAPL",
            name = "Apple Inc.",
            notes = null,
            createdAt = now,
            currentPrice = 178.50,
            currentPriceUpdatedAt = now,
            archived = false
        ),
        Asset(
            id = "2",
            accountId = "acc1",
            ticker = "MSFT",
            name = "Microsoft Corporation",
            notes = null,
            createdAt = now,
            currentPrice = 420.00,
            currentPriceUpdatedAt = now,
            archived = false
        ),
        Asset(
            id = "3",
            accountId = "acc1",
            ticker = "GOOGL",
            name = "Alphabet Inc.",
            notes = null,
            createdAt = now,
            currentPrice = 175.00,
            currentPriceUpdatedAt = now,
            archived = false
        )
    )
}

@Preview
@Composable
private fun AddDividendBottomSheetWithAssetSelectorPreview() {
    N3toTheme {
        AddDividendBottomSheet(
            fixedAssetName = null,
            allAssets = createMockAssets(),
            onSave = { _, _, _, _ -> },
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun AddDividendBottomSheetFixedAssetPreview() {
    N3toTheme {
        AddDividendBottomSheet(
            fixedAssetName = "Apple Inc.",
            allAssets = emptyList(),
            onSave = { _, _, _, _ -> },
            onDismiss = {}
        )
    }
}
