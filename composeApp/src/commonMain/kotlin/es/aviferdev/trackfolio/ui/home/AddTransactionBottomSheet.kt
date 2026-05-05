package es.aviferdev.trackfolio.ui.home

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    onDismiss: () -> Unit,
    viewModel: AddTransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AddTransactionUiState.Success) {
            onDismiss()
            viewModel.clear()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFBDBDBD))
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
                text = if (viewModel.isEditing) "Editar movimiento" else "Importe",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Spacer(Modifier.height(12.dp))

            val amountColor = if (viewModel.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
            val prefix = if (viewModel.type == TransactionType.INCOME) "+ " else "- "

            AmountInput(
                value = viewModel.amount,
                onValueChange = { viewModel.onAmountChange(it) },
                prefix = prefix,
                color = amountColor
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeChip(
                    label = "Ingreso",
                    selected = viewModel.type == TransactionType.INCOME,
                    selectedColor = IncomeGreen,
                    onClick = { viewModel.onTypeChange(TransactionType.INCOME) }
                )
                TypeChip(
                    label = "Gasto",
                    selected = viewModel.type == TransactionType.EXPENSE,
                    selectedColor = ExpenseRed,
                    onClick = { viewModel.onTypeChange(TransactionType.EXPENSE) }
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Categoría", fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.categories) { category ->
                        CategoryChip(
                            label = category.name,
                            selected = category.id == viewModel.selectedCategoryId,
                            onClick = { viewModel.onCategoryChange(category.id) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Nota (opcional)", fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    placeholder = {
                        Text(
                            text = "Ej. Compra supermercado",
                            color = TextSecondary.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDark,
                        unfocusedBorderColor = BorderGray
                    ),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(28.dp))

            if (uiState is AddTransactionUiState.Error) {
                Text(
                    text = (uiState as AddTransactionUiState.Error).message,
                    color = ExpenseRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    viewModel.save()
                          },
                enabled = viewModel.isValid && uiState !is AddTransactionUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryDark,
                    disabledContainerColor = PrimaryDark.copy(alpha = 0.38f)
                )
            ) {
                if (uiState is AddTransactionUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (uiState is AddTransactionUiState.Loading) "" else if (viewModel.isEditing) "Guardar cambios" else "Guardar", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun AmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    prefix: String,
    color: Color
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle = TextStyle(
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Transparent,
            textAlign = TextAlign.Center
        ),
        decorationBox = {
            Text(
                text = if (value.isEmpty()) "${prefix}0,00 €" else "$prefix$value €",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = if (value.isEmpty()) color.copy(alpha = 0.35f) else color,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TypeChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) selectedColor else Color.Transparent)
            .border(1.dp, if (selected) selectedColor else BorderGray, RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else TextSecondary
        )
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) SurfaceWhite else Color.Transparent)
            .border(
                width = if (selected) 1.5.dp else 0.5.dp,
                color = if (selected) PrimaryDark else BorderGray,
                shape = RoundedCornerShape(50.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) TextPrimary else TextSecondary
        )
    }
}
