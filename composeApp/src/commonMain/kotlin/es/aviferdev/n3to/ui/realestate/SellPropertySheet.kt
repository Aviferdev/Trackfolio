package es.aviferdev.n3to.ui.realestate

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
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
import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.PropertyExpense
import es.aviferdev.n3to.ui.theme.*

import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_action_cd
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.realestate_add_sale_expense
import n3to.composeapp.generated.resources.realestate_confirm_sale
import n3to.composeapp.generated.resources.realestate_net_proceeds
import n3to.composeapp.generated.resources.realestate_no_sale_expenses
import n3to.composeapp.generated.resources.realestate_sale_date_title
import n3to.composeapp.generated.resources.realestate_sale_expenses_short
import n3to.composeapp.generated.resources.realestate_sale_expenses_title_alt
import n3to.composeapp.generated.resources.realestate_sale_price_hint
import n3to.composeapp.generated.resources.realestate_sale_price_short
import n3to.composeapp.generated.resources.realestate_sale_summary
import n3to.composeapp.generated.resources.realestate_sell_price
import n3to.composeapp.generated.resources.realestate_sell_title
import org.jetbrains.compose.resources.stringResource
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Bottom sheet para registrar la venta de una propiedad.
 * Incluye precio de venta, fecha y gastos de venta con categorías.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellPropertySheet(
    propertyName: String,
    propertyId: String,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (saleDate: Long, saleValue: Double, expenses: List<PropertyExpense>) -> Unit
) {
    var saleValueText by remember { mutableStateOf("") }
    var saleDateMillis by remember { mutableStateOf(nowMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var expenses by remember { mutableStateOf(listOf<PropertyExpense>()) }
    var isLoading by remember { mutableStateOf(false) }

    val saleValue = saleValueText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val expenseCategories = categories.filter { it.name != "Ajuste de saldo" }

    val isValid = saleValue > 0 && saleDateMillis > 0

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = saleDateMillis)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { saleDateMillis = it }
                    showDatePicker = false
                }) {
                    Text(
                        stringResource(Res.string.common_accept),
                        color = MaterialTheme.appColors.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.textTertiary
                    )
                }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.appColors.surface)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.appColors.primary,
                    todayDateBorderColor = MaterialTheme.appColors.primary,
                    containerColor = MaterialTheme.appColors.surface
                )
            )
        }
    }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                stringResource(Res.string.realestate_sell_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Text(
                propertyName,
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textTertiary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ── Precio de venta ─────────────────────────────────────────────
            Text(
                stringResource(Res.string.realestate_sell_price),
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textSecondary
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = saleValueText,
                onValueChange = {
                    saleValueText = it.filter { c -> c.isDigit() || c == ',' || c == '.' }
                },
                placeholder = {
                    Text(
                        stringResource(Res.string.realestate_sale_price_hint),
                        color = MaterialTheme.appColors.textTertiary.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                trailingIcon = {
                    Text(
                        "€",
                        fontSize = 16.sp,
                        color = MaterialTheme.appColors.textSecondary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.appColors.primary,
                    unfocusedBorderColor = MaterialTheme.appColors.border,
                    cursorColor = MaterialTheme.appColors.primary,
                    focusedLabelColor = MaterialTheme.appColors.primary
                )
            )

            Spacer(Modifier.height(16.dp))

            // ── Fecha de venta ──────────────────────────────────────────────
            Text(
                stringResource(Res.string.realestate_sale_date_title),
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textSecondary
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = formatSellDate(saleDateMillis),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            stringResource(Res.string.common_action_cd),
                            tint = MaterialTheme.appColors.textTertiary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.appColors.primary,
                    unfocusedBorderColor = MaterialTheme.appColors.border,
                    disabledTextColor = MaterialTheme.appColors.textPrimary,
                    disabledBorderColor = MaterialTheme.appColors.border
                ),
                enabled = true
            )

            Spacer(Modifier.height(24.dp))

            // ── Gastos de venta ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.realestate_sale_expenses_title_alt),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.appColors.textPrimary
                )
                TextButton(
                    onClick = {
                        val firstCategory = expenseCategories.firstOrNull()?.id ?: ""
                        expenses =
                            expenses + PropertyExpense(categoryId = firstCategory, amount = 0.0)
                    }
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.appColors.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(Res.string.realestate_add_sale_expense),
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (expenses.isEmpty()) {
                Text(
                    stringResource(Res.string.realestate_no_sale_expenses),
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.textTertiary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                expenses.forEachIndexed { index, expense ->
                    PropertyExpenseRow(
                        categories = expenseCategories,
                        expense = expense,
                        onExpenseChange = { updated ->
                            expenses = expenses.toMutableList().apply { set(index, updated) }
                        },
                        onRemove = {
                            expenses = expenses.toMutableList().apply { removeAt(index) }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Resumen de la operación ─────────────────────────────────────
            if (saleValue > 0) {
                val totalExpenses = expenses.sumOf { it.amount }
                val netProceeds = saleValue - totalExpenses
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            stringResource(Res.string.realestate_sale_summary),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.appColors.textPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(Res.string.realestate_sale_price_short),
                                fontSize = 12.sp,
                                color = MaterialTheme.appColors.textSecondary
                            )
                            Text(
                                formatAmountEuro(saleValue),
                                fontSize = 12.sp,
                                color = MaterialTheme.appColors.income,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (totalExpenses > 0) {
                            Row(
                                Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    stringResource(Res.string.realestate_sale_expenses_short),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.appColors.textSecondary
                                )
                                Text(
                                    "-${formatAmountEuro(totalExpenses)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.appColors.expense,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        HorizontalDivider(
                            color = MaterialTheme.appColors.border2,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(Res.string.realestate_net_proceeds),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.appColors.textPrimary
                            )
                            Text(
                                formatAmountEuro(netProceeds),
                                fontWeight = FontWeight.Bold, fontSize = 13.sp,
                                color = if (netProceeds >= 0) MaterialTheme.appColors.income else MaterialTheme.appColors.expense
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Botón confirmar venta ──────────────────────────────────────
            Button(
                onClick = {
                    isLoading = true
                    val validExpenses = expenses.filter { it.amount > 0 }
                    onConfirm(saleDateMillis, saleValue, validExpenses)
                },
                enabled = isValid && !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.appColors.primary,
                    disabledContainerColor = MaterialTheme.appColors.primary.copy(alpha = 0.38f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = androidx.compose.ui.graphics.Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        stringResource(Res.string.realestate_confirm_sale),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

private fun formatSellDate(epochMillis: Long): String {
    val months = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
    val ld = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
}
