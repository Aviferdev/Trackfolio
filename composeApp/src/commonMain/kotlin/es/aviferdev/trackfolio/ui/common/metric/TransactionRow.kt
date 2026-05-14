package es.aviferdev.trackfolio.ui.common.metric

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
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
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.ui.common.toMaterialIcon
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import es.aviferdev.trackfolio.ui.theme.formatAmount
import es.aviferdev.trackfolio.ui.theme.formatDate
import es.aviferdev.trackfolio.ui.theme.maskAmount
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
        isAdjustment -> PrimaryDark
        isLinked -> PrimaryDark
        isIncome -> IncomeGreen
        else -> ExpenseRed
    }
    val avatarIcon = when {
        isAdjustment -> Icons.Outlined.Balance
        isLinked -> Icons.Outlined.ShowChart
        isIncome && !compact -> Icons.Outlined.ArrowDownward
        isIncome && compact && transaction.incomeType != null -> transaction.incomeType.toMaterialIcon()
        else -> null
    }
    val avatarText = when {
        avatarIcon != null -> null
        else -> label.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }
    val avatarContentDesc = when {
        isAdjustment -> "Ajuste"
        isLinked -> "Inversión"
        isIncome -> "Ingreso"
        else -> "Gasto"
    }

    // Importe
    val prefix = when {
        isAdjustment && transaction.amount >= 0 -> "+"
        isAdjustment -> "−"
        isIncome -> "+"
        else -> "−"
    }
    val amountColor = when {
        isAdjustment -> PrimaryDark
        isIncome -> IncomeGreen
        else -> ExpenseRed
    }
    val displayAmount = if (isAdjustment) abs(transaction.amount) else transaction.amount

    // Subtítulo
    val subtitle = when {
        isIncome && transaction.issuerName != null -> transaction.issuerName!!
        !compact && !transaction.notes.isNullOrBlank() -> transaction.notes
        else -> null
    }
    val dateFormatted = formatDate(transaction.date)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
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
                Text(avatarText ?: "?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(Modifier.width(12.dp))

        // Label + subtítulo
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
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
                            color = TextTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (subtitle != null && dateFormatted.isNotEmpty()) {
                        Spacer(Modifier.width(4.dp))
                        Text("·", fontSize = 11.sp, color = TextTertiary)
                        Spacer(Modifier.width(4.dp))
                    }
                    if (dateFormatted.isNotEmpty()) {
                        Text(
                            dateFormatted,
                            fontSize = 11.sp,
                            color = TextTertiary,
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
                        color = TextTertiary,
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
                    "Bruto: ${formatAmount(transaction.grossAmount)} €",
                    fontSize = 9.sp,
                    color = TextTertiary,
                    maxLines = 1
                )
            }
            if (!compact && isLinked) {
                Text("Portfolio", fontSize = 9.sp, color = PrimaryDark.copy(alpha = 0.6f))
            }
        }

        // Chevron en modo detallado
        if (!compact && onClick != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                "›",
                fontSize = 20.sp,
                color = TextTertiary,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Preview
@Composable
private fun TransactionRowCompactPreview() {
    TrackfolioTheme {
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
    TrackfolioTheme {
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
