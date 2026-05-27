package es.aviferdev.n3to.ui.realestate

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
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
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.PropertyExpense
import es.aviferdev.n3to.domain.model.PropertyType
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.n3to.domain.usecase.realestate.SavePropertyUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.common.component.SelectableChip
import es.aviferdev.n3to.ui.theme.*

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.realestate_acquisition_date_label
import n3to.composeapp.generated.resources.realestate_add_expense
import n3to.composeapp.generated.resources.realestate_add_property
import n3to.composeapp.generated.resources.realestate_name_label
import n3to.composeapp.generated.resources.realestate_type_label
import n3to.composeapp.generated.resources.realestate_address_label
import n3to.composeapp.generated.resources.realestate_address_placeholder
import n3to.composeapp.generated.resources.realestate_change_mortgage
import n3to.composeapp.generated.resources.realestate_current_value_label
import n3to.composeapp.generated.resources.realestate_edit_title
import n3to.composeapp.generated.resources.realestate_link_mortgage
import n3to.composeapp.generated.resources.realestate_monthly_rent_label
import n3to.composeapp.generated.resources.realestate_name_placeholder
import n3to.composeapp.generated.resources.realestate_no_expenses
import n3to.composeapp.generated.resources.realestate_ownership_label
import n3to.composeapp.generated.resources.realestate_purchase_expenses_title
import n3to.composeapp.generated.resources.realestate_purchase_price_label
import n3to.composeapp.generated.resources.realestate_rental_status_label
import n3to.composeapp.generated.resources.realestate_save_changes
import n3to.composeapp.generated.resources.common_action_cd
import n3to.composeapp.generated.resources.realestate_total_expenses_format
import n3to.composeapp.generated.resources.realestate_new_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPropertyBottomSheet(
    existingProperty: RealEstateProperty? = null,
    accountId: String,
    availableLoans: List<Loan>,
    onDismiss: () -> Unit,
    onSave: (RealEstateProperty, List<PropertyExpense>) -> Unit,
    savePropertyUseCase: SavePropertyUseCase = koinInject(),
    getCategoriesByType: GetCategoriesByTypeUseCase = koinInject(),
    session: AccountSession = koinInject()
) {
    val isEditing = existingProperty != null
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(existingProperty?.name ?: "") }
    var address by remember { mutableStateOf(existingProperty?.address ?: "") }
    var selectedPropertyType by remember {
        mutableStateOf(
            existingProperty?.propertyType ?: PropertyType.PRIMARY_HOME
        )
    }
    var purchaseValueText by remember {
        mutableStateOf(
            existingProperty?.purchaseValue?.toString() ?: ""
        )
    }
    var estimatedValueText by remember {
        mutableStateOf(
            existingProperty?.currentEstimatedValue?.toString() ?: ""
        )
    }
    var acquisitionDateMillis by remember {
        mutableStateOf(
            existingProperty?.acquisitionDate ?: nowMillis()
        )
    }
    var ownershipText by remember {
        mutableStateOf(
            existingProperty?.ownershipPercentage?.toString() ?: "100"
        )
    }
    var selectedRentalStatus by remember {
        mutableStateOf(
            existingProperty?.rentalStatus ?: RentalStatus.OWN_USE
        )
    }
    var monthlyRentText by remember {
        mutableStateOf(
            existingProperty?.monthlyRent?.toString() ?: ""
        )
    }
    var selectedLoanId by remember { mutableStateOf(existingProperty?.linkedLoanId) }
    var showLoanPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    // Gastos de compra
    var showPurchaseExpenses by remember { mutableStateOf(isEditing) }
    var purchaseExpenses by remember { mutableStateOf(listOf<PropertyExpense>()) }
    var expenseCategories by remember { mutableStateOf(listOf<Category>()) }
    var categoriesLoaded by remember { mutableStateOf(false) }
    val currency = LocalCurrencySymbol.current

    LaunchedEffect(accountId) {
        if (!categoriesLoaded && accountId.isNotBlank()) {
            expenseCategories =
                getCategoriesByType(accountId, TransactionType.EXPENSE).firstOrNull() ?: emptyList()
            categoriesLoaded = true
        }
    }

    val validExpenseCategories = expenseCategories.filter { it.name != "Ajuste de saldo" }

    val purchaseValue = purchaseValueText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val estimatedValue = estimatedValueText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val ownership = ownershipText.replace(',', '.').toDoubleOrNull() ?: 100.0
    val monthlyRent = monthlyRentText.replace(',', '.').toDoubleOrNull()

    val isValid = name.isNotBlank() && address.isNotBlank() &&
            purchaseValue > 0 && estimatedValue > 0 &&
            ownership in 0.0..100.0 &&
            (selectedRentalStatus != RentalStatus.RENTED || (monthlyRent != null && monthlyRent > 0))

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = acquisitionDateMillis)

    if (showLoanPicker) {
        LoanPickerSheet(
            loans = availableLoans, selectedLoanId = selectedLoanId,
            onLoanSelected = { loan -> selectedLoanId = loan?.id; showLoanPicker = false },
            onDismiss = { showLoanPicker = false }
        )
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        acquisitionDateMillis = it
                    }; showDatePicker = false
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
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(containerColor = MaterialTheme.appColors.surface)
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
                if (isEditing) stringResource(Res.string.realestate_edit_title) else stringResource(
                    Res.string.realestate_new_title
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.appColors.textPrimary
            )

            Spacer(Modifier.height(20.dp))

            // ── Nombre ───────────────────────────────────────────────────────
            SectionLabel(stringResource(Res.string.realestate_name_label))
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                placeholder = {
                    Text(
                        stringResource(Res.string.realestate_name_placeholder),
                        color = MaterialTheme.appColors.textTertiary.copy(alpha = 0.6f)
                    )
                },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(12.dp))

            // ── Dirección ────────────────────────────────────────────────────
            SectionLabel(stringResource(Res.string.realestate_address_label))
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = address, onValueChange = { address = it },
                placeholder = {
                    Text(
                        stringResource(Res.string.realestate_address_placeholder),
                        color = MaterialTheme.appColors.textTertiary.copy(alpha = 0.6f)
                    )
                },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(12.dp))

            // ── Tipo de vivienda ─────────────────────────────────────────────
            SectionLabel(stringResource(Res.string.realestate_type_label))
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PropertyType.entries) { type ->
                    SelectableChip(
                        label = "${type.emoji} ${type.label}",
                        selected = selectedPropertyType == type,
                        onClick = { selectedPropertyType = type }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Valor de compra + Valor estimado ─────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = purchaseValueText, onValueChange = { purchaseValueText = it },
                    label = { Text(stringResource(Res.string.realestate_purchase_price_label, currency)) },
                    singleLine = true, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp), colors = fieldColors()
                )
                OutlinedTextField(
                    value = estimatedValueText, onValueChange = { estimatedValueText = it },
                    label = { Text(stringResource(Res.string.realestate_current_value_label, currency)) },
                    singleLine = true, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp), colors = fieldColors()
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Fecha de adquisición ─────────────────────────────────────────
            SectionLabel(stringResource(Res.string.realestate_acquisition_date_label))
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = formatDate(acquisitionDateMillis),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            stringResource(Res.string.common_action_cd),
                            tint = MaterialTheme.appColors.textTertiary
                        )
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            // ── % de propiedad ───────────────────────────────────────────────
            SectionLabel(stringResource(Res.string.realestate_ownership_label))
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = ownershipText, onValueChange = { ownershipText = it },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(10.dp), colors = fieldColors()
            )

            Spacer(Modifier.height(12.dp))

            // ── Estado de alquiler ───────────────────────────────────────────
            SectionLabel(stringResource(Res.string.realestate_rental_status_label))
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RentalStatus.entries) { status ->
                    SelectableChip(
                        label = "${status.emoji} ${status.label}",
                        selected = selectedRentalStatus == status,
                        onClick = { selectedRentalStatus = status }
                    )
                }
            }

            if (selectedRentalStatus == RentalStatus.RENTED) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = monthlyRentText, onValueChange = { monthlyRentText = it },
                    label = { Text(stringResource(Res.string.realestate_monthly_rent_label, currency)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp), colors = fieldColors()
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Vincular hipoteca ────────────────────────────────────────────
            OutlinedButton(
                onClick = { showLoanPicker = true },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.appColors.primary),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.appColors.primary)
                )
            ) {
                Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (selectedLoanId != null) stringResource(Res.string.realestate_change_mortgage) else stringResource(
                        Res.string.realestate_link_mortgage
                    ),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Gastos de compra (expandible) ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f)
                        .clickable { showPurchaseExpenses = !showPurchaseExpenses },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(Res.string.realestate_purchase_expenses_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (showPurchaseExpenses) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.appColors.textTertiary
                    )
                }
                if (showPurchaseExpenses) {
                    TextButton(
                        onClick = {
                            val firstCat = validExpenseCategories.firstOrNull()?.id ?: ""
                            purchaseExpenses = purchaseExpenses + PropertyExpense(
                                categoryId = firstCat,
                                amount = 0.0
                            )
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
                            stringResource(Res.string.realestate_add_expense),
                            fontSize = 12.sp,
                            color = MaterialTheme.appColors.primary
                        )
                    }
                }
            }

            if (showPurchaseExpenses) {
                Spacer(Modifier.height(8.dp))
                if (purchaseExpenses.isEmpty()) {
                    Text(
                        stringResource(Res.string.realestate_no_expenses),
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.textTertiary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    purchaseExpenses.forEachIndexed { index, expense ->
                        PropertyExpenseRow(
                            categories = validExpenseCategories,
                            expense = expense,
                            onExpenseChange = { updated ->
                                purchaseExpenses =
                                    purchaseExpenses.toMutableList().apply { set(index, updated) }
                            },
                            onRemove = {
                                purchaseExpenses =
                                    purchaseExpenses.toMutableList().apply { removeAt(index) }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                if (purchaseExpenses.any { it.amount > 0 }) {
                    val totalPurchaseCosts = purchaseExpenses.sumOf { it.amount }
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(
                            stringResource(
                                Res.string.realestate_total_expenses_format,
                                formatAmountEuro(totalPurchaseCosts, currency)
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.appColors.textPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Botón guardar ───────────────────────────────────────────────
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val effectiveAccountId = if (accountId.isNotBlank()) accountId
                        else session.selectedAccountId.value ?: run {
                            isLoading = false
                            return@launch
                        }
                        val property = RealEstateProperty(
                            id = existingProperty?.id ?: uuid4().toString(),
                            accountId = effectiveAccountId,
                            name = name.trim(),
                            address = address.trim(),
                            propertyType = selectedPropertyType,
                            purchaseValue = purchaseValue,
                            currentEstimatedValue = estimatedValue,
                            acquisitionDate = acquisitionDateMillis,
                            ownershipPercentage = ownership,
                            linkedLoanId = selectedLoanId,
                            rentalStatus = selectedRentalStatus,
                            monthlyRent = if (selectedRentalStatus == RentalStatus.RENTED) monthlyRent else null,
                            mortgageReminderDismissed = existingProperty?.mortgageReminderDismissed
                                ?: false,
                            archived = existingProperty?.archived ?: false
                        )
                        val validExpenses = purchaseExpenses.filter { it.amount > 0 }
                        savePropertyUseCase(property, validExpenses)
                            .onSuccess { onSave(property, validExpenses) }
                        isLoading = false
                    }
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
                        Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (isEditing) stringResource(Res.string.realestate_save_changes) else stringResource(
                            Res.string.realestate_add_property
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary)
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.appColors.primary,
    unfocusedBorderColor = MaterialTheme.appColors.border,
    cursorColor = MaterialTheme.appColors.primary,
    focusedLabelColor = MaterialTheme.appColors.primary,
    unfocusedLabelColor = MaterialTheme.appColors.textTertiary,
    focusedTextColor = MaterialTheme.appColors.textPrimary,
    unfocusedTextColor = MaterialTheme.appColors.textPrimary
)

private fun formatDate(epochMillis: Long): String {
    val local =
        Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.dayOfMonth}/${local.monthNumber}/${local.year}"
}
