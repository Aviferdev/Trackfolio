package es.aviferdev.trackfolio.ui.portfolio.assethistory

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.TextTertiary

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = TextTertiary,
        letterSpacing = .7.sp,
        modifier = modifier
    )
}