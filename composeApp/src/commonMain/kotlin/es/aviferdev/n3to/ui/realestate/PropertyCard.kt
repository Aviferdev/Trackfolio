package es.aviferdev.n3to.ui.realestate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.PropertyType
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.ui.common.DeltaIndicator
import es.aviferdev.n3to.ui.common.ProgressBar
import es.aviferdev.n3to.ui.common.StatusTag
import es.aviferdev.n3to.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PropertyCard(
    property: RealEstateProperty,
    linkedLoan: Loan?,
    onClick: () -> Unit,
    showMortgageReminder: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(property.propertyType.emoji, fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(property.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(property.address, fontSize = 11.sp, color = TextTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (property.isSold) {
                        Text(formatAmountEuro(property.saleValue ?: 0.0), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IncomeGreen)
                        DeltaIndicator(
                            value = formatPercentSigned(property.realizedGainPercent ?: 0.0),
                            isPositive = (property.realizedGain ?: 0.0) >= 0
                        )
                    } else {
                        Text(formatAmountEuro(property.effectiveValue), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IncomeGreen)
                        DeltaIndicator(value = formatPercentSigned(property.unrealizedGainPercent), isPositive = property.unrealizedGain >= 0)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (property.isSold) {
                    StatusTag(label = "\u2705 Vendida", color = IncomeGreen)
                } else {
                    StatusTag(
                        label = when (property.rentalStatus) {
                            RentalStatus.RENTED -> "\uD83D\uDCB0 ${formatPercent(property.grossRentalYieldOnCurrent)}% yield"
                            RentalStatus.VACANT -> "\uD83D\uDD12 Vacía"
                            RentalStatus.OWN_USE -> "\uD83C\uDFE0 Uso propio"
                        },
                        color = when (property.rentalStatus) {
                            RentalStatus.RENTED -> IncomeGreen; RentalStatus.VACANT -> WarnAmber; RentalStatus.OWN_USE -> TextTertiary
                        }
                    )
                }
                if (showMortgageReminder) {
                    StatusTag(label = "\uD83C\uDFE0 Sin hipoteca", color = WarnAmber)
                }
            }
            if (linkedLoan != null) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Hipoteca: ${formatAmountEuro(linkedLoan.outstandingPrincipal)}", fontSize = 10.sp, color = TextTertiary)
                    Text("${linkedLoan.paidInstallments}/${linkedLoan.totalInstallments}", fontSize = 10.sp, color = TextTertiary)
                }
                Spacer(Modifier.height(4.dp))
                ProgressBar(progress = linkedLoan.progressPercent, color = ExpenseRed, height = 3.dp)
            }
        }
    }
}

@Preview
@Composable
private fun PropertyCardPreview() {
    N3toTheme {
        Column(modifier = Modifier.padding(16.dp).background(NavyDeep)) {
            PropertyCard(
                property = RealEstateProperty(
                    id = "1", accountId = "acc1", name = "Mi casa", address = "Calle Mayor 1, Madrid",
                    propertyType = PropertyType.PRIMARY_HOME, purchaseValue = 250000.0, currentEstimatedValue = 260000.0,
                    acquisitionDate = 1672531200000, ownershipPercentage = 100.0, linkedLoanId = "loan1",
                    rentalStatus = RentalStatus.OWN_USE, monthlyRent = null,
                    mortgageReminderDismissed = false, archived = false
                ),
                linkedLoan = Loan(
                    id = "loan1", accountId = "acc1", name = "Hipoteca BBVA",
                    type = es.aviferdev.n3to.domain.model.LoanType.MORTGAGE,
                    totalAmount = 200000.0, outstandingPrincipal = 150000.0,
                    currentInterestRate = 3.5, monthlyPayment = 950.0,
                    totalInstallments = 360, paidInstallments = 60,
                    startDate = 1672531200000, endDate = 1972531200000,
                    lenderName = "BBVA", notes = null, archived = false, createdAt = 1672531200000
                ),
                onClick = {}, showMortgageReminder = false
            )
        }
    }
}
