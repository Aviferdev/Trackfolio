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
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.TransactionType
import org.jetbrains.compose.ui.tooling.preview.Preview

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
            label = "Últimos movimientos",
            actionLabel = "Ver todos",
            onAction = onVerTodos,
            modifier = Modifier.padding(0.dp)
        )
        Spacer(Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
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
                            "Sin movimientos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Pulsa + para añadir tu primer movimiento",
                            fontSize = 12.sp,
                            color = TextTertiary,
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
                                color = NavyBorder,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun resolveTransactionLabel(
    transaction: Transaction,
    categoryNames: Map<String, String>
): String {
    if (transaction.isLinkedToAsset) return transaction.notes ?: "Inversión"
    if (transaction.isAdjustment) return "Ajuste de saldo"
    return if (transaction.isIncome) {
        transaction.incomeType?.label ?: "Ingreso"
    } else {
        transaction.categoryId?.let { categoryNames[it] } ?: "Gasto"
    }
}

@Preview
@Composable
private fun RecentTransactionsSectionPreview() {
    N3toTheme {
        RecentTransactionsSection(
            transactions = listOf(
                Transaction(
                    id = "1", accountId = "1", amount = 2500.0, type = TransactionType.INCOME,
                    categoryId = null, date = 1715500800000L, notes = null, createdAt = 1715500800000L,
                    incomeType = IncomeType.SALARY, grossAmount = 3000.0,
                    issuerName = "Empresa S.L."
                ),
                Transaction(
                    id = "2", accountId = "1", amount = 85.50, type = TransactionType.EXPENSE,
                    categoryId = "food", date = 1715414400000L, notes = null, createdAt = 1715414400000L
                )
            ),
            categoryNames = mapOf("food" to "Alimentación"),
            balancesHidden = false,
            onVerTodos = {}
        )
    }
}
