package es.aviferdev.n3to.ui.realestate

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.theme.*
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import org.jetbrains.compose.ui.tooling.preview.Preview
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.realestate_linked_transactions
import org.jetbrains.compose.resources.stringResource

@Composable
fun PropertyTransactionsSection(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val currency = LocalCurrencySymbol.current
    if (transactions.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(label = stringResource(Res.string.realestate_linked_transactions))
            Spacer(Modifier.height(8.dp))

            transactions.forEachIndexed { index, tx ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            if (tx.isIncome) "Ingreso" else tx.categoryId ?: "Gasto",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.appColors.textPrimary
                        )
                        Text(
                            formatDateShort(tx.date),
                            fontSize = 11.sp, color = MaterialTheme.appColors.textTertiary
                        )
                    }
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                        Text(
                            "${if (tx.isIncome) "+" else "-"}${formatAmountEuro(kotlin.math.abs(tx.amount), currency)}",
                            fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                            color = if (tx.isIncome) MaterialTheme.appColors.income else MaterialTheme.appColors.expense
                        )
                        tx.notes?.let {
                            Text(
                                it,
                                fontSize = 10.sp,
                                color = MaterialTheme.appColors.textTertiary
                            )
                        }
                    }
                }
                if (index < transactions.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.appColors.border,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PropertyTransactionsSectionPreview() {
    N3toTheme {
        PropertyTransactionsSection(
            transactions = listOf(
                Transaction(
                    id = "1",
                    accountId = "a1",
                    amount = 1200.0,
                    type = es.aviferdev.n3to.domain.model.TransactionType.INCOME,
                    categoryId = null,
                    date = 1747000000000,
                    notes = "Alquiler mayo",
                    createdAt = 1747000000000
                ),
                Transaction(
                    id = "2",
                    accountId = "a1",
                    amount = 50.0,
                    type = es.aviferdev.n3to.domain.model.TransactionType.EXPENSE,
                    categoryId = "cat_exp_03",
                    date = 1747000000000,
                    notes = "Comunidad",
                    createdAt = 1747000000000
                )
            )
        )
    }
}
