package es.aviferdev.n3to.ui.common

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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

import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.N3toTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Thin progress bar (3-4dp height).
 * Matches the `Bar` primitive from the JSX design system.
 *
 * Usage:
 * ```
 * ProgressBar(progress = 0.35f)                    // 35%
 * ProgressBar(progress = 0.72f, color = MaterialTheme.appColors.income, height = 4.dp)
 * ```
 *
 * @param progress Value between 0.0 and 1.0 (clamped internally to [0, 1]).
 * @param color    Fill color.
 * @param height   Total bar height.
 */
@Composable
fun ProgressBar(
    progress: Float,
    color: Color = MaterialTheme.appColors.primary,
    height: Dp = 3.dp,
    modifier: Modifier = Modifier
) {
    val clamped = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height))
            .background(MaterialTheme.appColors.border2)
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
    N3toTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            ProgressBar(progress = 0.35f)
            ProgressBar(progress = 0.72f, color = MaterialTheme.appColors.income, height = 4.dp)
            ProgressBar(progress = 1f, color = MaterialTheme.appColors.primary)
        }
    }
}
