package es.aviferdev.n3to.ui.common.metric

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatDateLocalized
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs

/**
 * Fila de transacción unificada. Soporta modo compacto (para HomeScreen)
 * y modo detallado (para TransactionListScreen).
 *
 * @param transaction Modelo de dominio de la transacción.
 * @param label Texto principal (nombre de categoría o resuelto externamente).
 * @param balancesHidden Si los saldos deben ocultarse.
 * @param compact Si es true, muestra versión simplificada (sin chevron, sin IncomeBadge).
 * @param onClick Acción al hacer click (null = no clickable).
 * @param modifier Modifier para personalizar.
 */
@Composable
fun TransactionRow(
    transaction: Transaction,
    label: String,
    balancesHidden: Boolean,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.isIncome
    val isAdjustment = transaction.isAdjustment
    val isLinked = transaction.isLinkedToAsset

    // Avatar
    val avatarBg = when {
        isAdjustment -> MaterialTheme.appColors.primary
        isLinked -> MaterialTheme.appColors.primary
        isIncome -> MaterialTheme.appColors.income
        else -> MaterialTheme.appColors.expense
    }
    val avatarIcon = when {
        isAdjustment -> Icons.Outlined.Balance
        isLinked -> Icons.AutoMirrored.Outlined.ShowChart
        isIncome && !compact -> Icons.Outlined.ArrowDownward
        isIncome && compact && transaction.incomeType != null -> transaction.incomeType.toMaterialIcon()
        else -> null
    }
    val avatarText = when {
        avatarIcon != null -> null
        else -> label.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }
    val avatarContentDesc = when {
        isAdjustment -> stringResource(Res.string.transaction_type_adjustment)
        isLinked -> stringResource(Res.string.transaction_label_investment)
        isIncome -> stringResource(Res.string.transaction_type_income)
        else -> stringResource(Res.string.transaction_type_expense)
    }

    // Importe
    val prefix = when {
        isAdjustment && transaction.amount >= 0 -> "+"
        isAdjustment -> "−"
        isIncome -> "+"
        else -> "−"
    }
    val amountColor = when {
        isAdjustment -> MaterialTheme.appColors.primary
        isIncome -> MaterialTheme.appColors.income
        else -> MaterialTheme.appColors.expense
    }
    val displayAmount = if (isAdjustment) abs(transaction.amount) else transaction.amount

    // Subtítulo
    val subtitle = when {
        isIncome && transaction.issuerName != null -> transaction.issuerName
        !compact && !transaction.notes.isNullOrBlank() -> transaction.notes
        else -> null
    }
    val dateFormatted = formatDateLocalized(transaction.date)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(if (compact) RoundedCornerShape(12.dp) else RoundedCornerShape(10.dp))
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            if (avatarIcon != null) {
                Icon(
                    avatarIcon,
                    contentDescription = avatarContentDesc,
                    tint = Color.White,
                    modifier = Modifier.size(if (compact) 22.dp else 20.dp)
                )
            } else {
                Text(
                    avatarText ?: "?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Label + subtítulo
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!compact) {
                Spacer(Modifier.height(1.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.appColors.textTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (subtitle != null && dateFormatted.isNotEmpty()) {
                        Spacer(Modifier.width(4.dp))
                        Text("·", fontSize = 11.sp, color = MaterialTheme.appColors.textTertiary)
                        Spacer(Modifier.width(4.dp))
                    }
                    if (dateFormatted.isNotEmpty()) {
                        Text(
                            dateFormatted,
                            fontSize = 11.sp,
                            color = MaterialTheme.appColors.textTertiary,
                            maxLines = 1
                        )
                    }
                }
            } else {
                // Modo compacto: solo fecha como subtítulo
                if (dateFormatted.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        dateFormatted,
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        // Importe
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$prefix ${maskAmount(formatAmount(displayAmount), balancesHidden)} €",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                maxLines = 1
            )
            if (!compact && isIncome && transaction.grossAmount != null && !balancesHidden) {
                Text(
                    stringResource(Res.string.transaction_gross_format, formatAmount(transaction.grossAmount)),
                    fontSize = 9.sp,
                    color = MaterialTheme.appColors.textTertiary,
                    maxLines = 1
                )
            }
            if (!compact && isLinked) {
                Text(
                    stringResource(Res.string.transaction_portfolio_badge),
                    fontSize = 9.sp,
                    color = MaterialTheme.appColors.primary.copy(alpha = 0.6f)
                )
            }
        }

        // Chevron en modo detallado
        if (!compact && onClick != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                "›",
                fontSize = 20.sp,
                color = MaterialTheme.appColors.textTertiary,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Preview
@Composable
private fun TransactionRowCompactPreview() {
    N3toTheme {
        TransactionRow(
            transaction = Transaction(
                id = "1", accountId = "acc1", amount = 1500.0, type = TransactionType.INCOME,
                categoryId = null, date = 1700000000000L, notes = null, createdAt = 1700000000000L
            ),
            label = "Nómina",
            balancesHidden = false,
            compact = true
        )
    }
}

@Preview
@Composable
private fun TransactionRowDetailPreview() {
    N3toTheme {
        TransactionRow(
            transaction = Transaction(
                id = "2", accountId = "acc1", amount = 45.50, type = TransactionType.EXPENSE,
                categoryId = "cat2", date = 1700000000000L, notes = "Cena con amigos",
                createdAt = 1700000000000L
            ),
            label = "Restaurante",
            balancesHidden = false,
            compact = false,
            onClick = {}
        )
    }
}
