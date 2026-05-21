package es.aviferdev.n3to.ui.transaction.components

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.TaxRole
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.ui.common.N3toLabel

import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.PrimaryAlpha
import es.aviferdev.n3to.ui.theme.PrimaryDark

import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatDate
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_search_cd
import n3to.composeapp.generated.resources.realestate_detail_title
import n3to.composeapp.generated.resources.transaction_filter_expense
import n3to.composeapp.generated.resources.transaction_filter_income
import n3to.composeapp.generated.resources.transaction_label_expense
import n3to.composeapp.generated.resources.transaction_label_income
import n3to.composeapp.generated.resources.transaction_label_investment
import n3to.composeapp.generated.resources.transaction_search_hint
import n3to.composeapp.generated.resources.transaction_type_adjustment
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchBar(query: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.appColors.surface)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = stringResource(Res.string.common_search_cd),
            tint = MaterialTheme.appColors.textTertiary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onChange,
            singleLine = true,
            modifier = Modifier.weight(1f),
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.appColors.textPrimary,
                fontSize = 12.sp
            ),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        stringResource(Res.string.transaction_search_hint),
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.textTertiary
                    )
                }
                inner()
            }
        )
        if (query.isNotBlank()) {
            TextButton(
                onClick = { onChange("") },
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text("×", fontSize = 16.sp, color = MaterialTheme.appColors.textTertiary)
            }
        }
    }
}

@Composable
internal fun TotalsRow(totalIncome: Double, totalExpense: Double, balancesHidden: Boolean) {
    val balance = totalIncome - totalExpense
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TotalCell(
                label = stringResource(Res.string.transaction_filter_income),
                amount = totalIncome,
                color = MaterialTheme.appColors.income,
                prefix = "+",
                balancesHidden = balancesHidden,
                modifier = Modifier.weight(1f)
            )
            Box(Modifier.width(1.dp).height(40.dp).background(MaterialTheme.appColors.border))
            TotalCell(
                label = stringResource(Res.string.transaction_filter_expense),
                amount = totalExpense,
                color = MaterialTheme.appColors.expense,
                prefix = "−",
                balancesHidden = balancesHidden,
                modifier = Modifier.weight(1f)
            )
            Box(Modifier.width(1.dp).height(40.dp).background(MaterialTheme.appColors.border))
            TotalCell(
                label = "Balance", amount = kotlin.math.abs(balance),
                color = if (balance >= 0) MaterialTheme.appColors.primary else MaterialTheme.appColors.expense,
                prefix = if (balance >= 0) "+" else "−",
                balancesHidden = balancesHidden, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TotalCell(
    label: String, amount: Double, color: Color, prefix: String,
    balancesHidden: Boolean, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        N3toLabel(text = label, modifier = Modifier.padding(bottom = 4.dp))
        Text(
            "$prefix ${maskAmount(formatAmount(amount), balancesHidden)} €",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun TransactionCard(
    transaction: Transaction,
    label: String,
    balancesHidden: Boolean,
    onEdit: (() -> Unit)?,
    onClick: (() -> Unit)? = null,
) {
    val isIncome = transaction.isIncome
    val isAdjustment = transaction.isAdjustment
    val isLinkedAsset = transaction.isLinkedToAsset
    val isLinkedProperty = transaction.linkedPropertyId != null
    val isLinked = isLinkedAsset || isLinkedProperty
    val avatarBg = when {
        isAdjustment -> MaterialTheme.appColors.primary; isLinked -> MaterialTheme.appColors.primary
        isIncome -> MaterialTheme.appColors.income; else -> MaterialTheme.appColors.expense
    }
    val avatarIcon = when {
        isAdjustment -> Icons.Outlined.SwapHoriz
        isLinkedAsset -> Icons.AutoMirrored.Outlined.ShowChart
        isLinkedProperty -> Icons.Outlined.House
        isIncome -> Icons.Outlined.ArrowDownward
        else -> Icons.Outlined.ArrowUpward
    }
    val avatarContentDesc = when {
        isAdjustment -> stringResource(Res.string.transaction_type_adjustment)
        isLinkedAsset -> stringResource(Res.string.transaction_label_investment)
        isLinkedProperty -> stringResource(Res.string.realestate_detail_title)
        isIncome -> stringResource(Res.string.transaction_label_income)
        else -> stringResource(Res.string.transaction_label_expense)
    }
    val prefix = when {
        isAdjustment && transaction.amount >= 0 -> "+"
        isAdjustment -> "−"; isIncome -> "+"; else -> "−"
    }
    val amountColor = when {
        isAdjustment -> MaterialTheme.appColors.primary; isIncome -> MaterialTheme.appColors.income; else -> MaterialTheme.appColors.expense
    }
    val displayAmount =
        if (isAdjustment) kotlin.math.abs(transaction.amount) else transaction.amount
    val subtitle = when {
        isIncome && transaction.issuerName != null -> transaction.issuerName
        !transaction.notes.isNullOrBlank() -> transaction.notes
        else -> null
    }
    val dateFormatted = formatDate(transaction.date)

    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.appColors.surface)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = avatarIcon,
                contentDescription = avatarContentDesc,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null || dateFormatted.isNotEmpty()) {
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
            }
            if (isIncome && transaction.incomeType != null && transaction.grossAmount != null) {
                Spacer(Modifier.height(2.dp))
                IncomeBadge(transaction)
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "$prefix ${maskAmount(formatAmount(displayAmount), balancesHidden)} €",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                maxLines = 1
            )
            if (isIncome && transaction.grossAmount != null && !balancesHidden) {
                Text(
                    "Bruto: ${formatAmount(transaction.grossAmount)} €",
                    fontSize = 9.sp,
                    color = MaterialTheme.appColors.textTertiary,
                    maxLines = 1
                )
            }
            if (isLinked) {
                Text(
                    "Portfolio",
                    fontSize = 9.sp,
                    color = MaterialTheme.appColors.primary.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            "›",
            fontSize = 20.sp,
            color = MaterialTheme.appColors.textTertiary,
            fontWeight = FontWeight.Light,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

internal fun incomeTypeIcon(incomeType: IncomeType): ImageVector = when (incomeType) {
    IncomeType.SALARY -> Icons.Outlined.Badge
    IncomeType.BANK_INTEREST -> Icons.Outlined.AccountBalance
    IncomeType.BOND_DEPOSIT -> Icons.Outlined.RequestQuote
    IncomeType.DIVIDEND -> Icons.AutoMirrored.Outlined.ShowChart
    IncomeType.BONUS_PRIZE -> Icons.Outlined.CardGiftcard
    IncomeType.PRIZE_LOTTERY -> Icons.Outlined.EmojiEvents
    IncomeType.RENTAL_INCOME -> Icons.Outlined.House
    IncomeType.FREELANCE -> Icons.Outlined.BusinessCenter
    IncomeType.EXEMPT_INCOME -> Icons.Outlined.CheckCircle
}

@Composable
internal fun IncomeBadge(transaction: Transaction) {
    val incType = transaction.incomeType ?: return
    val pct = transaction.taxLines.firstOrNull { it.role == TaxRole.INCOME_TAX }?.percent
    Row(
        modifier = Modifier
            .background(PrimaryAlpha, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = incomeTypeIcon(incType),
            contentDescription = incType.label,
            tint = MaterialTheme.appColors.primary,
            modifier = Modifier.size(11.dp)
        )
        val text = buildString {
            append(incType.label)
            if (pct != null && pct > 0) append(" · ${pct.toLong()}% retención")
        }
        Text(
            text,
            fontSize = 8.sp,
            color = MaterialTheme.appColors.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwipeToDeleteContainer(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState()
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            state.reset()
        }
    }
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val bg by animateColorAsState(
                targetValue = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.appColors.expense else Color.Transparent,
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier.fillMaxSize().background(bg).padding(end = 18.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    "Eliminar",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    ) { Surface(color = MaterialTheme.appColors.surface) { content() } }
}
