package es.aviferdev.n3to.ui.realestate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.LoanType
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanPickerSheet(
    loans: List<Loan>,
    selectedLoanId: String?,
    onLoanSelected: (Loan?) -> Unit,
    onDismiss: () -> Unit
) {
    var filterMortgageOnly by remember { mutableStateOf(false) }
    val filteredLoans = if (filterMortgageOnly) {
        loans.filter { it.type == LoanType.MORTGAGE }
    } else {
        loans
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFBDBDBD))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Título
            Text(
                "Seleccionar préstamo",
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp,
                color      = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Vincula una hipoteca o préstamo existente",
                fontSize = 13.sp,
                color    = TextTertiary
            )

            Spacer(Modifier.height(12.dp))

            // Filtro solo hipotecas
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = filterMortgageOnly,
                    onCheckedChange = { filterMortgageOnly = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryDark)
                )
                Spacer(Modifier.width(4.dp))
                Text("Solo hipotecas", fontSize = 13.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(8.dp))

            // Opción "Ninguno"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLoanSelected(null) }
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedLoanId == null,
                    onClick  = { onLoanSelected(null) },
                    colors   = RadioButtonDefaults.colors(selectedColor = PrimaryDark)
                )
                Spacer(Modifier.width(8.dp))
                Text("Ninguno", fontSize = 14.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(4.dp))

            // Lista de préstamos
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(filteredLoans, key = { it.id }) { loan ->
                    LoanPickerItem(
                        loan       = loan,
                        isSelected = loan.id == selectedLoanId,
                        onClick    = { onLoanSelected(loan) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoanPickerItem(
    loan: Loan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick  = onClick,
            colors   = RadioButtonDefaults.colors(selectedColor = PrimaryDark)
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = loan.type.toMaterialIcon(),
            contentDescription = null,
            tint        = PrimaryDark,
            modifier    = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(loan.name, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TextPrimary)
            loan.lenderName?.let {
                Text(it, fontSize = 11.sp, color = TextTertiary)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatAmountEuro(loan.outstandingPrincipal),
                fontWeight = FontWeight.SemiBold,
                fontSize   = 12.sp,
                color      = ExpenseRed
            )
            Text(
                "${formatAmountEuro(loan.monthlyPayment)}/mes",
                fontSize   = 10.sp,
                color      = TextTertiary
            )
        }
    }
    HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
}

@Preview
@Composable
private fun LoanPickerSheetPreview() {
    N3toTheme {
        LoanPickerSheet(
            loans = listOf(
                Loan(
                    id = "1", accountId = "acc1", name = "Hipoteca BBVA",
                    type = LoanType.MORTGAGE, totalAmount = 200000.0,
                    outstandingPrincipal = 150000.0, currentInterestRate = 3.5,
                    monthlyPayment = 950.0, totalInstallments = 360, paidInstallments = 60,
                    startDate = 1672531200000, endDate = 1972531200000,
                    lenderName = "BBVA", notes = null, archived = false, createdAt = 1672531200000
                ),
                Loan(
                    id = "2", accountId = "acc1", name = "Préstamo coche",
                    type = LoanType.CAR, totalAmount = 25000.0,
                    outstandingPrincipal = 12000.0, currentInterestRate = 5.0,
                    monthlyPayment = 450.0, totalInstallments = 60, paidInstallments = 24,
                    startDate = 1672531200000, endDate = 1704067200000,
                    lenderName = "Santander", notes = null, archived = false, createdAt = 1672531200000
                )
            ),
            selectedLoanId = "1",
            onLoanSelected = {},
            onDismiss = {}
        )
    }
}
