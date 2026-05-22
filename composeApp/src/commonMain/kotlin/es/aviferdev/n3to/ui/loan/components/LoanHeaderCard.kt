package es.aviferdev.n3to.ui.loan.components

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatPercent
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.fixedincome_interest_label
import n3to.composeapp.generated.resources.loan_monthly_payment
import n3to.composeapp.generated.resources.loan_pending_capital
import n3to.composeapp.generated.resources.loan_progress
import n3to.composeapp.generated.resources.loan_term_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoanHeaderCard(loan: Loan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.primary),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Text(
                    "${loan.type.emoji} ${loan.type.label}",
                    fontSize = 12.sp,
                    color = Color.White.copy(.5f)
                )
                loan.lenderName?.let { Text(it, fontSize = 12.sp, color = Color.White.copy(.5f)) }
            }
            Spacer(Modifier.height(10.dp))

            // Saldo pendiente
            Text(
                stringResource(Res.string.loan_pending_capital),
                fontSize = 11.sp,
                color = Color.White.copy(.5f)
            )
            Text(
                "−${formatAmount(loan.outstandingPrincipal)} €",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(.12f), thickness = .5.dp)
            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                HeroMetric(
                    stringResource(Res.string.loan_monthly_payment),
                    "${formatAmount(loan.monthlyPayment)} €"
                )
                HeroMetric(
                    stringResource(Res.string.fixedincome_interest_label),
                    "${formatPercent(loan.currentInterestRate)}%"
                )
                HeroMetric(
                    stringResource(Res.string.loan_term_label),
                    "${loan.totalInstallments} meses"
                )
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(.12f), thickness = .5.dp)
            Spacer(Modifier.height(10.dp))

            // Progress bar
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(
                    stringResource(Res.string.loan_progress),
                    fontSize = 10.sp,
                    color = Color.White.copy(.45f)
                )
                Text(
                    "${loan.paidInstallments}/${loan.totalInstallments} cuotas · ${(loan.progressPercent * 100).toInt()}%",
                    fontSize = 10.sp, color = Color.White.copy(.45f)
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { loan.progressPercent },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = Color.White.copy(.9f),
                trackColor = Color.White.copy(.2f)
            )
        }
    }
}

@Composable
fun HeroMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.White.copy(.45f))
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
