package es.aviferdev.n3to.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.N3toTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Rounded-square avatar with centered initials/emoji.
 * Matches the `Avatar` primitive from the JSX design system.
 *
 * Usage:
 * ```
 * InitialsAvatar("A", bgColor = ExpenseRed, size = 40.dp, textSize = 15.sp)
 * InitialsAvatar("📈", bgColor = PrimaryDark)
 * ```
 */
@Composable
fun InitialsAvatar(
    text: String,
    bgColor: Color = PrimaryDark,
    size: Dp = 40.dp,
    textSize: Int = 11,
    modifier: Modifier = Modifier
) {
    Box(
        modifier         = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = text.take(3),
            fontSize  = textSize.sp,
            fontWeight = FontWeight.ExtraBold,
            color     = Color.White,
            textAlign = TextAlign.Center,
            maxLines  = 1
        )
    }
}

@Preview
@Composable
private fun InitialsAvatarPreview() {
    N3toTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InitialsAvatar("A", bgColor = PrimaryDark)
            InitialsAvatar("📈", bgColor = IncomeGreen)
            InitialsAvatar("JD", bgColor = ExpenseRed, size = 48.dp, textSize = 14)
        }
    }
}
