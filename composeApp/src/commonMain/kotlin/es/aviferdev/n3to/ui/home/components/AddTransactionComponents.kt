package es.aviferdev.n3to.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.ui.home.viewmodel.IrpfInputMode
import es.aviferdev.n3to.ui.theme.PrimaryAlpha
import es.aviferdev.n3to.ui.theme.appColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_date_label
import n3to.composeapp.generated.resources.transaction_net_estimated
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TypePill(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) selectedColor else Color.Transparent)
            .border(
                1.dp,
                if (selected) selectedColor else MaterialTheme.appColors.border,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else MaterialTheme.appColors.textSecondary
        )
    }
}

@Composable
internal fun ModeChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.appColors.primary.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = if (selected) 1.5.dp else 0.5.dp,
                color = if (selected) MaterialTheme.appColors.primary else MaterialTheme.appColors.border,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        label()
    }
}

@Composable
internal fun DarkTappableRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.appColors.surfaceElevated)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryAlpha),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.appColors.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (value.contains("…")) MaterialTheme.appColors.textTertiary else MaterialTheme.appColors.textPrimary
            )
        }

        Text("›", fontSize = 20.sp, color = MaterialTheme.appColors.textTertiary)
    }
}

@Composable
internal fun DarkAmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    color: Color,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.appColors.textSecondary
        )
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Transparent,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp
            ),
            decorationBox = {
                Text(
                    text = if (value.isEmpty()) "0,00 €" else "$value €",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (value.isEmpty()) MaterialTheme.appColors.textTertiary else color,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-1).sp,
                    lineHeight = 36.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.appColors.surfaceElevated)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.appColors.textPrimary),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        fontSize = 13.sp,
                        color = MaterialTheme.appColors.textTertiary
                    )
                }
                inner()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateRow(
    dateMillis: Long,
    onDateSelected: (Long) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    val instant = Instant.fromEpochMilliseconds(dateMillis)
    val ld = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val months = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
    val dateText = "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"

    DarkTappableRow(
        icon = Icons.Outlined.CalendarMonth,
        label = stringResource(Res.string.common_date_label),
        value = dateText,
        onClick = { showPicker = true }
    )

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { selectedUtc ->
                        val selectedLocal = Instant.fromEpochMilliseconds(selectedUtc)
                            .toLocalDateTime(TimeZone.UTC).date
                        val localInstant =
                            selectedLocal.atStartOfDayIn(TimeZone.currentSystemDefault())
                        onDateSelected(localInstant.toEpochMilliseconds())
                    }
                    showPicker = false
                }) {
                    Text(
                        stringResource(Res.string.common_accept),
                        color = MaterialTheme.appColors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.textSecondary
                    )
                }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.appColors.surface)
        ) {
            DatePicker(
                state = pickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.appColors.primary,
                    todayDateBorderColor = MaterialTheme.appColors.primary
                )
            )
        }
    }
}

@Composable
internal fun DarkInlineField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    suffix: String = "",
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.appColors.textSecondary
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.appColors.surfaceElevated)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Transparent
                ),
                decorationBox = { inner ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        inner()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (value.isEmpty()) {
                                Text(
                                    placeholder,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.appColors.textTertiary
                                )
                            } else {
                                Text(
                                    value,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.appColors.textPrimary
                                )
                            }
                            if (suffix.isNotEmpty()) {
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    suffix,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.appColors.textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun IrpfCompactField(
    label: String,
    irpfInputMode: IrpfInputMode,
    onIrpfInputModeChange: (IrpfInputMode) -> Unit,
    irpfPercent: String,
    onIrpfPercentChange: (String) -> Unit,
    irpfFixedAmount: String,
    onIrpfFixedAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.appColors.textSecondary
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.appColors.surfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = if (irpfInputMode == IrpfInputMode.PERCENT) irpfPercent else irpfFixedAmount,
                    onValueChange = {
                        if (irpfInputMode == IrpfInputMode.PERCENT) onIrpfPercentChange(it) else onIrpfFixedAmountChange(
                            it
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Transparent
                    ),
                    decorationBox = { inner ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            inner()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val currentValue =
                                    if (irpfInputMode == IrpfInputMode.PERCENT) irpfPercent else irpfFixedAmount
                                if (currentValue.isEmpty()) {
                                    Text(
                                        if (irpfInputMode == IrpfInputMode.PERCENT) "0 %" else "0,00 €",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.appColors.textTertiary
                                    )
                                } else {
                                    Text(
                                        currentValue,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.appColors.textPrimary
                                    )
                                }
                                Text(
                                    if (irpfInputMode == IrpfInputMode.PERCENT) "%" else "€",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.appColors.textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IrpfModeChip(
                label = "%",
                selected = irpfInputMode == IrpfInputMode.PERCENT,
                onClick = { onIrpfInputModeChange(IrpfInputMode.PERCENT) }
            )
            IrpfModeChip(
                label = "€",
                selected = irpfInputMode == IrpfInputMode.AMOUNT,
                onClick = { onIrpfInputModeChange(IrpfInputMode.AMOUNT) }
            )
        }
    }
}

@Composable
private fun IrpfModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.appColors.primary.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = if (selected) 1.5.dp else 0.5.dp,
                color = if (selected) MaterialTheme.appColors.primary else MaterialTheme.appColors.border,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.appColors.primary else MaterialTheme.appColors.textSecondary
        )
    }
}

@Composable
internal fun CalculatedNetRow(net: Double) {
    val formatted = formatAmount(net)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.appColors.income.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.transaction_net_estimated),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.income
            )
            Text(
                "$formatted €",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.income
            )
        }
    }
}

@Composable
internal fun IssuerSelector(
    issuers: List<Issuer>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    issuerTypeLabel: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            issuerTypeLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.appColors.textSecondary
        )
        Spacer(Modifier.height(8.dp))
        if (issuers.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(issuers) { issuer ->
                    IssuerChip(
                        name = issuer.name,
                        icon = issuer.icon,
                        selected = issuer.id == selectedId,
                        onClick = { onSelect(issuer.id) }
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.appColors.surfaceElevated
            ) {
                Text(
                    "Sin emisores. Añádelos en Ajustes.",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun IssuerChip(name: String, icon: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) MaterialTheme.appColors.surface else Color.Transparent)
            .border(
                if (selected) 1.5.dp else 0.5.dp,
                if (selected) MaterialTheme.appColors.primary else MaterialTheme.appColors.border,
                RoundedCornerShape(50.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                name,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textSecondary
            )
        }
    }
}

internal fun formatAmount(amount: Double): String {
    val negative = amount < 0
    val abs = if (negative) -amount else amount
    val rounded = (abs * 100).toLong()
    val euros = rounded / 100
    val cents = rounded % 100
    val eurosStr = euros.toString().reversed().chunked(3).joinToString(".").reversed()
    return if (negative) "-$eurosStr,${
        cents.toString().padStart(2, '0')
    }" else "$eurosStr,${cents.toString().padStart(2, '0')}"
}
