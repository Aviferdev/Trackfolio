package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.toMaterialIcon

import es.aviferdev.n3to.ui.theme.N3toTheme
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_fixed_income_section
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FixedIncomeSectionHeader(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon("🏦".toMaterialIcon(), contentDescription = null, tint = MaterialTheme.appColors.textPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(Res.string.portfolio_fixed_income_section),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.appColors.textPrimary
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = "($count)",
            fontSize = 11.sp,
            color = MaterialTheme.appColors.textTertiary
        )
    }
}

@Preview
@Composable
private fun FixedIncomeSectionHeaderPreview() {
    N3toTheme {
        FixedIncomeSectionHeader(count = 3)
    }
}
