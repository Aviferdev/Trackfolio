package es.aviferdev.n3to.ui.debt.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.aviferdev.n3to.domain.model.DebtDirection
import es.aviferdev.n3to.ui.common.InitialsAvatar
import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors

@Composable
fun DebtDirectionBadge(
    personName: String,
    direction: DebtDirection,
    size: Dp = 38.dp,
    textSize: Int = 15,
    modifier: Modifier = Modifier
) {
    InitialsAvatar(
        text = personName.firstOrNull()?.uppercase() ?: "?",
        bgColor = if (direction == DebtDirection.THEY_OWE) MaterialTheme.appColors.income else MaterialTheme.appColors.expense,
        size = size,
        textSize = textSize,
        modifier = modifier
    )
}
