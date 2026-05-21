package es.aviferdev.n3to.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.metric.TransactionRow
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.home_no_movements_subtitle
import n3to.composeapp.generated.resources.home_no_movements_title
import n3to.composeapp.generated.resources.home_section_recent
import n3to.composeapp.generated.resources.home_view_all
import n3to.composeapp.generated.resources.transaction_label_balance_adjustment
import n3to.composeapp.generated.resources.transaction_label_expense
import n3to.composeapp.generated.resources.transaction_label_income
import n3to.composeapp.generated.resources.transaction_label_investment
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecentTransactionsSection(
    transactions: List<Transaction>,
    categoryNames: Map<String, String>,
    balancesHidden: Boolean,
    onVerTodos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(
            label = stringResource(Res.string.home_section_recent),
            actionLabel = stringResource(Res.string.home_view_all),
            onAction = onVerTodos,
            modifier = Modifier.padding(0.dp)
        )
        Spacer(Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(Res.string.home_no_movements_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.appColors.textPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(Res.string.home_no_movements_subtitle),
                            fontSize = 12.sp,
                            color = MaterialTheme.appColors.textTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                Column {
                    transactions.forEachIndexed { index, tx ->
                        TransactionRow(
                            transaction = tx,
                            label = resolveTransactionLabel(tx, categoryNames),
                            balancesHidden = balancesHidden,
                            compact = true
                        )
                        if (index < transactions.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 68.dp),
                                color = MaterialTheme.appColors.navyBorder,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun resolveTransactionLabel(
    transaction: Transaction,
    categoryNames: Map<String, String>
): String {
    if (transaction.isLinkedToAsset) return transaction.notes
        ?: stringResource(Res.string.transaction_label_investment)
    if (transaction.isAdjustment) return stringResource(Res.string.transaction_label_balance_adjustment)
    return if (transaction.isIncome) {
        transaction.incomeType?.label ?: stringResource(Res.string.transaction_label_income)
    } else {
        transaction.categoryId?.let { categoryNames[it] }
            ?: stringResource(Res.string.transaction_label_expense)
    }
}