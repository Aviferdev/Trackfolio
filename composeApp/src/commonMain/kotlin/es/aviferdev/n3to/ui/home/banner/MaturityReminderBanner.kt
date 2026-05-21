package es.aviferdev.n3to.ui.home.banner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.FixedIncomeType
import es.aviferdev.n3to.domain.model.InterestFrequency
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.home_maturity_more_format
import n3to.composeapp.generated.resources.home_maturity_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MaturityReminderBanner(
    positions: List<FixedIncomePosition>,
    visible: Boolean,
    onDismiss: () -> Unit,
    onViewDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.appColors.warnAmber.copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.warnAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.home_maturity_title),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.warnAmber,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.appColors.warnAmber.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Position rows
            val shown = positions.take(3)
            shown.forEach { position ->
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewDetails(position.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        position.type.toMaterialIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.appColors.warnAmber
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = position.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.appColors.warnAmber,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${position.remainingDays}d",
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.warnAmber.copy(alpha = 0.7f)
                    )
                }
            }

            if (positions.size > 3) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.home_maturity_more_format, positions.size - 3),
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.warnAmber.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 22.dp)
                )
            }
        }
    }
}

private fun createMockPositions(): List<FixedIncomePosition> {
    val now = nowMillis()
    val dayInMillis = 24 * 60 * 60 * 1000L
    return listOf(
        FixedIncomePosition(
            id = "1",
            accountId = "acc1",
            name = "Bono Tesoro 2025",
            ticker = "ES0000000001",
            type = FixedIncomeType.BOND,
            notes = null,
            principal = 10000.0,
            quantity = 10.0,
            nominalPerUnit = 1000.0,
            interestRate = 3.5,
            interestFrequency = InterestFrequency.ANNUAL,
            startDate = now - (180 * dayInMillis),
            maturityDate = now + (15 * dayInMillis),
            platformId = "platform1",
            issuerId = null,
            autoRenew = false,
            archived = false,
            closedAt = null,
            closeType = null,
            feeNote = null,
            createdAt = now - (180 * dayInMillis)
        ),
        FixedIncomePosition(
            id = "2",
            accountId = "acc1",
            name = "Depósito Bankia",
            ticker = "DEP001",
            type = FixedIncomeType.DEPOSIT,
            notes = null,
            principal = 5000.0,
            quantity = 1.0,
            nominalPerUnit = 5000.0,
            interestRate = 2.0,
            interestFrequency = InterestFrequency.AT_MATURITY,
            startDate = now - (365 * dayInMillis),
            maturityDate = now + (7 * dayInMillis),
            platformId = "platform1",
            issuerId = null,
            autoRenew = false,
            archived = false,
            closedAt = null,
            closeType = null,
            feeNote = null,
            createdAt = now - (365 * dayInMillis)
        ),
        FixedIncomePosition(
            id = "3",
            accountId = "acc1",
            name = "Letra Tesoro",
            ticker = "LT001",
            type = FixedIncomeType.BILL,
            notes = null,
            principal = 3000.0,
            quantity = 3.0,
            nominalPerUnit = 1000.0,
            interestRate = 1.5,
            interestFrequency = InterestFrequency.AT_MATURITY,
            startDate = now - (90 * dayInMillis),
            maturityDate = now + (25 * dayInMillis),
            platformId = "platform1",
            issuerId = null,
            autoRenew = false,
            archived = false,
            closedAt = null,
            closeType = null,
            feeNote = null,
            createdAt = now - (90 * dayInMillis)
        )
    )
}

@Preview
@Composable
private fun MaturityReminderBannerVisiblePreview() {
    N3toTheme {
        MaturityReminderBanner(
            positions = createMockPositions(),
            visible = true,
            onDismiss = {},
            onViewDetails = {}
        )
    }
}

@Preview
@Composable
private fun MaturityReminderBannerHiddenPreview() {
    N3toTheme {
        MaturityReminderBanner(
            positions = emptyList(),
            visible = false,
            onDismiss = {},
            onViewDetails = {}
        )
    }
}
