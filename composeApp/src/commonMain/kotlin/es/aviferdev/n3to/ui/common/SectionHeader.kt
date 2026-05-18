package es.aviferdev.n3to.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.N3toTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SectionHeader(
    label: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp, start = 16.dp, end = 16.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        N3toLabel(text = label)
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyanAccent,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

@Preview
@Composable
private fun SectionHeaderPreview() {
    N3toTheme {
        SectionHeader(label = "Últimos movimientos")
    }
}

@Preview
@Composable
private fun SectionHeaderWithActionPreview() {
    N3toTheme {
        SectionHeader(
            label = "Activos",
            actionLabel = "Ver todos",
            onAction = {}
        )
    }
}
