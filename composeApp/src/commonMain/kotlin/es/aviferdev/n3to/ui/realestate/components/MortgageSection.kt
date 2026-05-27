package es.aviferdev.n3to.ui.realestate.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmountEuro
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.realestate_lender_label
import n3to.composeapp.generated.resources.realestate_loan_label
import n3to.composeapp.generated.resources.realestate_monthly_payment_label
import n3to.composeapp.generated.resources.realestate_mortgage_hint
import n3to.composeapp.generated.resources.realestate_mortgage_label
import n3to.composeapp.generated.resources.realestate_no_mortgage
import n3to.composeapp.generated.resources.realestate_pending_capital_label
import n3to.composeapp.generated.resources.realestate_view_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun MortgageSection(
    linkedLoan: Loan?,
    onNavigateToLoan: (String) -> Unit
) {
    val currency = LocalCurrencySymbol.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (linkedLoan != null) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(Res.string.realestate_mortgage_label),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    TextButton(onClick = { onNavigateToLoan(linkedLoan.id) }) {
                        Text(
                            stringResource(Res.string.realestate_view_label),
                            color = MaterialTheme.appColors.primary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                DataRow(stringResource(Res.string.realestate_loan_label), linkedLoan.name)
                linkedLoan.lenderName?.let {
                    DataRow(stringResource(Res.string.realestate_lender_label), it)
                }
                DataRow(
                    stringResource(Res.string.realestate_pending_capital_label),
                    formatAmountEuro(linkedLoan.outstandingPrincipal, currency)
                )
                DataRow(
                    stringResource(Res.string.realestate_monthly_payment_label),
                    formatAmountEuro(linkedLoan.monthlyPayment, currency)
                )
            } else {
                Text(
                    stringResource(Res.string.realestate_no_mortgage),
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
                Text(
                    stringResource(Res.string.realestate_mortgage_hint),
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }
        }
    }
}
