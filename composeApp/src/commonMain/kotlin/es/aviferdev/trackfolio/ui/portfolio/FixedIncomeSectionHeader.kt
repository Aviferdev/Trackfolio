package es.aviferdev.trackfolio.ui.portfolio

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
import es.aviferdev.trackfolio.ui.common.toMaterialIcon
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun FixedIncomeSectionHeader(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon("🏦".toMaterialIcon(), contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Renta fija",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = "($count)",
            fontSize = 11.sp,
            color = TextTertiary
        )
    }
}

@Preview
@Composable
private fun FixedIncomeSectionHeaderPreview() {
    TrackfolioTheme {
        FixedIncomeSectionHeader(count = 3)
    }
}
