package es.aviferdev.n3to.ui.realestate

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.ui.common.component.SelectableChip
import es.aviferdev.n3to.ui.theme.*

import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_action_cd
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.realestate_change_rental_status
import n3to.composeapp.generated.resources.realestate_change_rental_title
import n3to.composeapp.generated.resources.realestate_monthly_rent_label
import n3to.composeapp.generated.resources.common_confirm
import n3to.composeapp.generated.resources.realestate_change_rental_date
import n3to.composeapp.generated.resources.realestate_rental_status_label
import org.jetbrains.compose.resources.stringResource
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeRentalStatusSheet(
    currentStatus: RentalStatus,
    onDismiss: () -> Unit,
    onConfirm: (newStatus: RentalStatus, effectiveDate: Long, monthlyRent: Double?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var effectiveDateMillis by remember { mutableStateOf(nowMillis()) }
    var monthlyRentText by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val currency = LocalCurrencySymbol.current

    val monthlyRent = monthlyRentText.replace(',', '.').toDoubleOrNull()
    val isValid = selectedStatus != currentStatus &&
            (selectedStatus != RentalStatus.RENTED || (monthlyRent != null && monthlyRent > 0))

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = effectiveDateMillis)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { effectiveDateMillis = it }
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
                Modifier.padding(top = 12.dp, bottom = 4.dp).width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp)).background(MaterialTheme.appColors.dragHandle)
            )
        }
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Text(
                stringResource(Res.string.realestate_change_rental_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(Res.string.realestate_change_rental_status),
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textTertiary
            )
            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(Res.string.realestate_rental_status_label),
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textSecondary
            )
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RentalStatus.entries) { status ->
                    SelectableChip(
                        label = "${status.emoji} ${status.label}",
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Text(
                stringResource(Res.string.realestate_change_rental_date),
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textSecondary
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = formatDate(effectiveDateMillis), onValueChange = {}, readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.appColors.primary,
                    unfocusedBorderColor = MaterialTheme.appColors.border,
                    focusedTextColor = MaterialTheme.appColors.textPrimary,
                    unfocusedTextColor = MaterialTheme.appColors.textPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        showDatePicker = true
                    }) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            stringResource(Res.string.common_action_cd),
                            tint = MaterialTheme.appColors.textTertiary
                        )
                    }
                }
            )

            if (selectedStatus == RentalStatus.RENTED) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = monthlyRentText,
                    onValueChange = { monthlyRentText = it },
                    label = { Text(stringResource(Res.string.realestate_monthly_rent_label, currency)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.appColors.primary,
                        unfocusedBorderColor = MaterialTheme.appColors.border,
                        cursorColor = MaterialTheme.appColors.primary,
                        focusedLabelColor = MaterialTheme.appColors.primary,
                        unfocusedLabelColor = MaterialTheme.appColors.textTertiary,
                        focusedTextColor = MaterialTheme.appColors.textPrimary,
                        unfocusedTextColor = MaterialTheme.appColors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onConfirm(
                        selectedStatus,
                        effectiveDateMillis,
                        if (selectedStatus == RentalStatus.RENTED) monthlyRent else null
                    )
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.appColors.primary)
            ) { Text(stringResource(Res.string.common_confirm), fontWeight = FontWeight.Bold) }
        }
    }
}

private fun formatDate(epochMillis: Long): String {
    val local =
        Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.dayOfMonth}/${local.monthNumber}/${local.year}"
}
