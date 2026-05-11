package es.aviferdev.trackfolio.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.aviferdev.trackfolio.ui.theme.BorderGray2
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Thin progress bar (3-4dp height).
 * Matches the `Bar` primitive from the JSX design system.
 *
 * Usage:
 * ```
 * ProgressBar(progress = 0.35f)                    // 35%
 * ProgressBar(progress = 0.72f, color = IncomeGreen, height = 4.dp)
 * ```
 *
 * @param progress Value between 0.0 and 1.0 (clamped internally to [0, 1]).
 * @param color    Fill color.
 * @param height   Total bar height.
 */
@Composable
fun ProgressBar(
    progress: Float,
    color: Color = PrimaryDark,
    height: Dp = 3.dp,
    modifier: Modifier = Modifier
) {
    val clamped = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height))
            .background(BorderGray2)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clamped)
                .clip(RoundedCornerShape(height))
                .background(color)
        )
    }
}

@Preview
@Composable
private fun ProgressBarPreview() {
    TrackfolioTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            ProgressBar(progress = 0.35f)
            ProgressBar(progress = 0.72f, color = IncomeGreen, height = 4.dp)
            ProgressBar(progress = 1f, color = PrimaryDark)
        }
    }
}
