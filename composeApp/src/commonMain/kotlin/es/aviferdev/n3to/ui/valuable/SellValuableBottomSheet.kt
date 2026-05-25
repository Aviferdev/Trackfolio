package es.aviferdev.n3to.ui.valuable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.ValuableExpense
import es.aviferdev.n3to.domain.model.ValuableExpenseCategories
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.common.input.DatePickerRow
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.valuable_add_sell_expense
import n3to.composeapp.generated.resources.valuable_confirm_sale
import n3to.composeapp.generated.resources.valuable_expense_amount_label
import n3to.composeapp.generated.resources.valuable_expense_delete_cd
import n3to.composeapp.generated.resources.valuable_sell_date_label
import n3to.composeapp.generated.resources.valuable_sell_expenses_title
import n3to.composeapp.generated.resources.valuable_sell_price_label
import n3to.composeapp.generated.resources.valuable_sell_title_format
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellValuableBottomSheet(
    valuableId: String,
    valuableName: String,
    onDismiss: () -> Unit,
    onConfirm: (saleDate: Long, salePrice: Double, expenses: List<ValuableExpense>) -> Unit
) {
    var salePriceText by remember { mutableStateOf("") }
    var saleDateMillis by remember { mutableStateOf(nowMillis()) }
    var saleExpenses by remember { mutableStateOf(listOf<ValuableExpense>()) }
    val expenseCategories = remember { ValuableExpenseCategories.allIds.toList() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.appColors.navySurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(Res.string.valuable_sell_title_format, valuableName),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = salePriceText, onValueChange = { salePriceText = it },
                label = { Text(stringResource(Res.string.valuable_sell_price_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            DatePickerRow(
                label = stringResource(Res.string.valuable_sell_date_label),
                dateMillis = saleDateMillis,
                onDateSelected = { saleDateMillis = it }
            )
            Spacer(Modifier.height(16.dp))

            // Gastos de venta
            Text(
                stringResource(Res.string.valuable_sell_expenses_title),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            saleExpenses.forEachIndexed { index, expense ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var showCategoryMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        val catName = expense.categoryId.removePrefix("cat_exp_val_")
                        OutlinedButton(
                            onClick = { showCategoryMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.appColors.textPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(40.dp).fillMaxWidth()
                        ) {
                            Text(text = catName, fontSize = 12.sp, maxLines = 1)
                        }
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            expenseCategories.forEach { catId ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            catId.removePrefix("cat_exp_val_"),
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        saleExpenses = saleExpenses.toMutableList().apply {
                                            set(index, expense.copy(categoryId = catId))
                                        }
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = if (expense.amount > 0) expense.amount.toString() else "",
                        onValueChange = { text ->
                            val amt = text.toDoubleOrNull() ?: 0.0
                            saleExpenses = saleExpenses.toMutableList().apply {
                                set(index, expense.copy(amount = amt))
                            }
                        },
                        label = { Text(stringResource(Res.string.valuable_expense_amount_label)) },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f),
                        colors = textFieldColors()
                    )
                    IconButton(onClick = {
                        saleExpenses = saleExpenses.toMutableList().apply { removeAt(index) }
                    }) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = stringResource(Res.string.valuable_expense_delete_cd),
                            tint = MaterialTheme.appColors.expense
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
            TextButton(onClick = {
                saleExpenses = saleExpenses + ValuableExpense(
                    categoryId = expenseCategories.first(),
                    amount = 0.0
                )
            }) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(Res.string.valuable_add_sell_expense))
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val price = salePriceText.toDoubleOrNull() ?: return@Button
                    onConfirm(saleDateMillis, price, saleExpenses.filter { it.amount > 0 })
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.appColors.income)
            ) {
                Text(
                    stringResource(Res.string.valuable_confirm_sale),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.appColors.income,
    unfocusedBorderColor = MaterialTheme.appColors.border,
    focusedTextColor = MaterialTheme.appColors.textPrimary,
    unfocusedTextColor = MaterialTheme.appColors.textPrimary,
    cursorColor = MaterialTheme.appColors.income,
    focusedLabelColor = MaterialTheme.appColors.income,
    unfocusedLabelColor = MaterialTheme.appColors.textSecondary
)
