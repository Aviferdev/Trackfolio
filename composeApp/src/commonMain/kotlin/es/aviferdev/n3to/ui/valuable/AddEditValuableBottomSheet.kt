package es.aviferdev.n3to.ui.valuable

import androidx.compose.foundation.background
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
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.Valuable
import es.aviferdev.n3to.domain.model.ValuableExpense
import es.aviferdev.n3to.domain.model.ValuableExpenseCategories
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.common.input.DatePickerRow
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_save
import n3to.composeapp.generated.resources.portfolio_add_asset_title_edit
import n3to.composeapp.generated.resources.valuable_add_holding_expense
import n3to.composeapp.generated.resources.valuable_add_purchase_expense
import n3to.composeapp.generated.resources.valuable_description_label
import n3to.composeapp.generated.resources.valuable_description_placeholder
import n3to.composeapp.generated.resources.valuable_estimated_value_label
import n3to.composeapp.generated.resources.valuable_estimated_value_placeholder
import n3to.composeapp.generated.resources.valuable_expense_amount_label
import n3to.composeapp.generated.resources.valuable_expense_delete_cd
import n3to.composeapp.generated.resources.valuable_holding_expenses_title
import n3to.composeapp.generated.resources.valuable_list_title
import n3to.composeapp.generated.resources.valuable_name_label
import n3to.composeapp.generated.resources.valuable_name_placeholder
import n3to.composeapp.generated.resources.valuable_notes_label
import n3to.composeapp.generated.resources.valuable_purchase_date_label
import n3to.composeapp.generated.resources.valuable_purchase_expenses_title
import n3to.composeapp.generated.resources.valuable_purchase_price_label
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditValuableBottomSheet(
    existingValuable: Valuable? = null,
    existingPurchaseExpenses: List<ValuableExpense> = emptyList(),
    existingHoldingExpenses: List<ValuableExpense> = emptyList(),
    accountId: String,
    onDismiss: () -> Unit,
    onSave: (Valuable, List<ValuableExpense>, List<ValuableExpense>) -> Unit
) {
    val isEditing = existingValuable != null

    var name by remember { mutableStateOf(existingValuable?.name ?: "") }
    var description by remember { mutableStateOf(existingValuable?.description ?: "") }
    var purchasePriceText by remember {
        mutableStateOf(
            existingValuable?.purchasePrice?.toString() ?: ""
        )
    }
    var purchaseDateMillis by remember {
        mutableStateOf(
            existingValuable?.purchaseDate ?: nowMillis()
        )
    }
    var estimatedValueText by remember {
        mutableStateOf(
            existingValuable?.estimatedValue?.toString() ?: ""
        )
    }
    var purchaseExpenses by remember { mutableStateOf(existingPurchaseExpenses) }
    var holdingExpenses by remember { mutableStateOf(existingHoldingExpenses) }
    var notes by remember { mutableStateOf(existingValuable?.notes ?: "") }

    val expenseCategories = remember {
        ValuableExpenseCategories.allIds.toList()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.appColors.navySurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.navyBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (isEditing) stringResource(Res.string.portfolio_add_asset_title_edit) else stringResource(
                    Res.string.valuable_list_title
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(20.dp))

            // Nombre
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text(stringResource(Res.string.valuable_name_label)) },
                placeholder = { Text(stringResource(Res.string.valuable_name_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            // Descripción
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text(stringResource(Res.string.valuable_description_label)) },
                placeholder = { Text(stringResource(Res.string.valuable_description_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            // Precio compra
            OutlinedTextField(
                value = purchasePriceText, onValueChange = { purchasePriceText = it },
                label = { Text(stringResource(Res.string.valuable_purchase_price_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            // Fecha compra
            DatePickerRow(
                label = stringResource(Res.string.valuable_purchase_date_label),
                dateMillis = purchaseDateMillis,
                onDateSelected = { purchaseDateMillis = it }
            )
            Spacer(Modifier.height(12.dp))

            // Valor estimado
            OutlinedTextField(
                value = estimatedValueText, onValueChange = { estimatedValueText = it },
                label = { Text(stringResource(Res.string.valuable_estimated_value_label)) },
                placeholder = { Text(stringResource(Res.string.valuable_estimated_value_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            // Notas
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text(stringResource(Res.string.valuable_notes_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(Modifier.height(16.dp))

            // ── Gastos de compra ──────────────────────────────────────────
            Text(
                stringResource(Res.string.valuable_purchase_expenses_title),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            purchaseExpenses.forEachIndexed { index, expense ->
                ExpenseRow(
                    expense = expense,
                    categories = expenseCategories,
                    onExpenseChange = { newExpense ->
                        purchaseExpenses = purchaseExpenses.toMutableList().apply {
                            set(index, newExpense)
                        }
                    },
                    onRemove = {
                        purchaseExpenses = purchaseExpenses.toMutableList().apply {
                            removeAt(index)
                        }
                    }
                )
                Spacer(Modifier.height(6.dp))
            }
            TextButton(onClick = {
                purchaseExpenses = purchaseExpenses + ValuableExpense(
                    categoryId = expenseCategories.first(),
                    amount = 0.0
                )
            }) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(Res.string.valuable_add_purchase_expense))
            }
            Spacer(Modifier.height(12.dp))

            // ── Gastos de tenencia ────────────────────────────────────────
            Text(
                stringResource(Res.string.valuable_holding_expenses_title),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            holdingExpenses.forEachIndexed { index, expense ->
                ExpenseRow(
                    expense = expense,
                    categories = expenseCategories,
                    onExpenseChange = { newExpense ->
                        holdingExpenses = holdingExpenses.toMutableList().apply {
                            set(index, newExpense)
                        }
                    },
                    onRemove = {
                        holdingExpenses = holdingExpenses.toMutableList().apply {
                            removeAt(index)
                        }
                    }
                )
                Spacer(Modifier.height(6.dp))
            }
            TextButton(onClick = {
                holdingExpenses = holdingExpenses + ValuableExpense(
                    categoryId = expenseCategories.first(),
                    amount = 0.0
                )
            }) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(Res.string.valuable_add_holding_expense))
            }

            Spacer(Modifier.height(24.dp))

            // Botón guardar
            Button(
                onClick = {
                    val price = purchasePriceText.toDoubleOrNull() ?: return@Button
                    val estValue = estimatedValueText.toDoubleOrNull()
                    val valuable = Valuable(
                        id = existingValuable?.id ?: uuid4().toString(),
                        accountId = accountId,
                        name = name,
                        description = description,
                        purchasePrice = price,
                        purchaseDate = purchaseDateMillis,
                        estimatedValue = estValue,
                        salePrice = existingValuable?.salePrice,
                        saleDate = existingValuable?.saleDate,
                        linkedLoanId = existingValuable?.linkedLoanId,
                        notes = notes.ifBlank { null },
                        createdAt = existingValuable?.createdAt ?: nowMillis()
                    )
                    onSave(
                        valuable,
                        purchaseExpenses.filter { it.amount > 0 },
                        holdingExpenses.filter { it.amount > 0 })
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.appColors.cyanAccent,
                    contentColor = MaterialTheme.appColors.navyDeep
                )
            ) {
                Text(stringResource(Res.string.common_save), fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: ValuableExpense,
    categories: List<String>,
    onExpenseChange: (ValuableExpense) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category selector
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
                categories.forEach { catId ->
                    DropdownMenuItem(
                        text = { Text(catId.removePrefix("cat_exp_val_"), fontSize = 13.sp) },
                        onClick = {
                            onExpenseChange(expense.copy(categoryId = catId))
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
                onExpenseChange(expense.copy(amount = amt))
            },
            label = { Text(stringResource(Res.string.valuable_expense_amount_label)) },
            singleLine = true,
            modifier = Modifier.weight(0.7f),
            colors = textFieldColors()
        )
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = stringResource(Res.string.valuable_expense_delete_cd),
                tint = MaterialTheme.appColors.expense
            )
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.appColors.cyanAccent,
    unfocusedBorderColor = MaterialTheme.appColors.navyBorder,
    focusedTextColor = MaterialTheme.appColors.textPrimary,
    unfocusedTextColor = MaterialTheme.appColors.textPrimary,
    cursorColor = MaterialTheme.appColors.cyanAccent,
    focusedLabelColor = MaterialTheme.appColors.cyanAccent,
    unfocusedLabelColor = MaterialTheme.appColors.textSecondary,
    focusedContainerColor = MaterialTheme.appColors.navySurfaceLight,
    unfocusedContainerColor = MaterialTheme.appColors.navySurfaceLight
)
