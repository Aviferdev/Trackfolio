package es.aviferdev.n3to.ui.debt.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import es.aviferdev.n3to.domain.model.Debt
import es.aviferdev.n3to.domain.model.DebtDirection
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatDateLocalized
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_edit
import n3to.composeapp.generated.resources.debt_paid
import org.jetbrains.compose.resources.stringResource

@Composable
fun DebtCard(
    debt: Debt,
    hidden: Boolean,
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit
) {
    val currency = LocalCurrencySymbol.current
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
            DebtDirectionBadge(
                personName = debt.personName,
                direction = debt.direction
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    debt.personName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${debt.notes ?: "Sin nota"} · ${formatDateLocalized(debt.date)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textTertiary,
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${maskAmount(formatAmount(debt.amount), hidden)} $currency",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (debt.direction == DebtDirection.THEY_OWE) MaterialTheme.appColors.income else MaterialTheme.appColors.expense
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onMarkPaid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.appColors.income.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.appColors.income
                        ),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            stringResource(Res.string.debt_paid),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = onEdit,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.appColors.surface4.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.appColors.textSecondary
                        ),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(stringResource(Res.string.common_edit), fontSize = 9.sp)
                    }
                }
            }
        }
    }
}
