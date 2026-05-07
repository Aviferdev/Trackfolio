package es.aviferdev.trackfolio.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceElevated
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.formatAmount
import es.aviferdev.trackfolio.ui.theme.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceUpdateBottomSheet(
    outdatedAssets: List<Asset>,
    updatedAssetIds: Set<String>,
    onUpdatePrice: (assetId: String, newPrice: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val priceInputs = remember { mutableStateMapOf<String, String>() }
    val totalCount = outdatedAssets.size
    val completedCount = updatedAssetIds.size

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
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text       = "Actualizar precios",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                modifier   = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text     = "$completedCount de $totalCount actualizados",
                fontSize = 13.sp,
                color    = TextSecondary
            )

            // Barra de progreso visual
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderGray)
            ) {
                val fraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(IncomeGreen)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Lista de activos
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(outdatedAssets, key = { it.id }) { asset ->
                    val isCompleted = asset.id in updatedAssetIds
                    AssetPriceUpdateRow(
                        asset              = asset,
                        isCompleted        = isCompleted,
                        priceInput         = priceInputs[asset.id] ?: "",
                        onPriceInputChange = { priceInputs[asset.id] = it },
                        onConfirm = {
                            val value = priceInputs[asset.id]
                                ?.replace(',', '.')
                                ?.toDoubleOrNull() ?: return@AssetPriceUpdateRow
                            onUpdatePrice(asset.id, value)
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick  = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cerrar", fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun AssetPriceUpdateRow(
    asset: Asset,
    isCompleted: Boolean,
    priceInput: String,
    onPriceInputChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (isCompleted) SurfaceElevated else SurfaceWhite
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Badge del ticker
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCompleted) IncomeGreen else PrimaryDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completado",
                            tint     = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text       = asset.ticker.take(3),
                            fontSize   = if (asset.ticker.length > 3) 8.sp else 10.sp,
                            color      = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = asset.name,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = TextPrimary,
                        maxLines   = 1
                    )
                    Row {
                        Text(
                            text     = asset.ticker,
                            fontSize = 11.sp,
                            color    = TextSecondary
                        )
                        if (asset.currentPrice != null) {
                            Text(
                                text     = " · ${formatAmount(asset.currentPrice!!)} €",
                                fontSize = 11.sp,
                                color    = TextSecondary
                            )
                        }
                    }
                    if (asset.currentPriceUpdatedAt != null) {
                        Text(
                            text     = "Última: ${formatDate(asset.currentPriceUpdatedAt!!)}",
                            fontSize = 10.sp,
                            color    = TextSecondary.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text     = "Sin precio registrado",
                            fontSize = 10.sp,
                            color    = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (!isCompleted) {
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value         = priceInput,
                        onValueChange = { new ->
                            onPriceInputChange(new.filter { c -> c.isDigit() || c == ',' || c == '.' })
                        },
                        placeholder     = { Text("Nuevo precio", fontSize = 14.sp, color = TextSecondary.copy(alpha = 0.5f)) },
                        textStyle       = TextStyle(fontSize = 14.sp, color = TextPrimary),
                        modifier        = Modifier.weight(1f).height(48.dp),
                        shape           = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = PrimaryDark,
                            unfocusedBorderColor = BorderGray
                        ),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick  = onConfirm,
                        enabled  = priceInput.replace(',', '.').toDoubleOrNull()?.let { it >= 0 } == true,
                        colors   = IconButtonDefaults.iconButtonColors(
                            containerColor         = PrimaryDark,
                            contentColor           = Color.White,
                            disabledContainerColor = PrimaryDark.copy(alpha = 0.38f),
                            disabledContentColor   = Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Check, "Confirmar", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
