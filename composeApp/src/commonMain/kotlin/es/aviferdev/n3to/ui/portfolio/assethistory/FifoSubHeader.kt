package es.aviferdev.n3to.ui.portfolio.assethistory

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.appColors

@Composable
fun FifoSubHeader(text: String) {
    Text(
        text.uppercase(),
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.appColors.textTertiary,
        letterSpacing = .5.sp,
        modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 5.dp)
    )
}