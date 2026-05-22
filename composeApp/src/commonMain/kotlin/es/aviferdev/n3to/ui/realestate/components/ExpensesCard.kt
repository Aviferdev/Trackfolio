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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmountEuro
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.realestate_expense_default
import n3to.composeapp.generated.resources.realestate_purchase_expenses
import n3to.composeapp.generated.resources.realestate_sale_expenses
import org.jetbrains.compose.resources.stringResource

/**
 * Card que lista los gastos de compra de un inmueble.
 */
@Composable
fun PurchaseExpensesCard(expenses: List<Transaction>) {
    ExpensesCard(
        title = stringResource(Res.string.realestate_purchase_expenses),
        expenses = expenses
    )
}

/**
 * Card que lista los gastos de venta de un inmueble.
 */
@Composable
fun SaleExpensesCard(expenses: List<Transaction>) {
    ExpensesCard(
        title = stringResource(Res.string.realestate_sale_expenses),
        expenses = expenses
    )
}

@Composable
private fun ExpensesCard(
    title: String,
    expenses: List<Transaction>
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            expenses.forEach { tx ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        tx.categoryId ?: stringResource(Res.string.realestate_expense_default),
                        fontSize = 12.sp, color = MaterialTheme.appColors.textSecondary
                    )
                    Text(
                        "-${formatAmountEuro(kotlin.math.abs(tx.amount))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.expense,
                        fontWeight = FontWeight.Medium
                    )
                }
                tx.notes?.let { note ->
                    Text(
                        note,
                        fontSize = 10.sp,
                        color = MaterialTheme.appColors.textTertiary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
