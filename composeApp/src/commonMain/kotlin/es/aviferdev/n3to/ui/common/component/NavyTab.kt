package es.aviferdev.n3to.ui.common.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.N3toTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Tab del sistema de diseño Navy/Fintech.
 *
 * Patrón compartido por AccountSelectorBar y los filtros de gráficos.
 * Muestra una etiqueta de texto con un subrayado cian animado bajo el
 * item activo. Sin borde ni relleno de fondo — idéntico al AccountTab.
 *
 * @param leadingContent Contenido opcional antes del label (p.ej. avatar circular).
 */
@Composable
fun NavyTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null
) {
    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) 20.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navyTabIndicator"
    )

    Column(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            leadingContent?.invoke()
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.40f)
            )
        }

        Box(
            modifier = Modifier
                .height(2.dp)
                .width(indicatorWidth)
                .background(CyanAccent, RoundedCornerShape(1.dp))
        )
    }
}

@Preview
@Composable
private fun NavyTabPreview() {
    N3toTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            NavyTab(label = "1M", selected = false, onClick = {})
            NavyTab(label = "3M", selected = false, onClick = {})
            NavyTab(label = "6M", selected = true, onClick = {})
            NavyTab(label = "1A", selected = false, onClick = {})
            NavyTab(label = "Todo", selected = false, onClick = {})
        }
    }
}
