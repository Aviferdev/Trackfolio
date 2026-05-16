package es.aviferdev.n3to.ui.portfolio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.N3toTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ClosedPositionsHeader(
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✓", fontSize = 14.sp, color = TextTertiary)
            Spacer(Modifier.width(8.dp))
            Text(
                "Posiciones cerradas",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextTertiary
            )
            Spacer(Modifier.width(5.dp))
            Text("($count)", fontSize = 11.sp, color = TextTertiary)
        }
        Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (expanded) "Colapsar" else "Expandir",
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview
@Composable
private fun ClosedPositionsHeaderCollapsedPreview() {
    N3toTheme {
        ClosedPositionsHeader(count = 5, expanded = false, onToggle = {})
    }
}

@Preview
@Composable
private fun ClosedPositionsHeaderExpandedPreview() {
    N3toTheme {
        ClosedPositionsHeader(count = 3, expanded = true, onToggle = {})
    }
}
