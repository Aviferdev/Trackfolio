package es.aviferdev.n3to.ui.portfolio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.BorderGray
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.SurfaceElevated
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_add_tx_no_assets_hint
import n3to.composeapp.generated.resources.portfolio_add_tx_no_platforms_hint
import n3to.composeapp.generated.resources.portfolio_add_tx_no_units_badge
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TypeToggle(
    label: String,
    isSel: Boolean,
    selColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSel) selColor.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = if (isSel) 1.dp else 0.dp,
                color = if (isSel) selColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 14.sp,
            color      = if (isSel) selColor else TextSecondary,
            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
internal fun CategoryFilterChip(
    label: String,
    icon: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg     = if (isSelected) PrimaryDark   else SurfaceElevated
    val border = if (isSelected) PrimaryDark   else BorderGray
    val text   = if (isSelected) Color.White   else TextPrimary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Text(text = icon, fontSize = 12.sp, color = if (isSelected) Color.White else Color.Unspecified)
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text       = label,
            fontSize   = 12.sp,
            color      = text,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
internal fun AssetChip(
    ticker: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg     = if (isSelected) PrimaryDark   else SurfaceElevated
    val border = if (isSelected) PrimaryDark   else BorderGray
    val text   = if (isSelected) Color.White   else TextPrimary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = ticker, fontSize = 12.sp, color = text, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Text(text = name, fontSize = 12.sp, color = text.copy(alpha = if (isSelected) 0.85f else 0.65f))
    }
}

@Composable
internal fun PlatformChip(
    icon: String,
    label: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    badge: String? = null,
    onClick: () -> Unit
) {
    val bg = when {
        !enabled   -> SurfaceElevated.copy(alpha = 0.5f)
        isSelected -> PrimaryDark
        else       -> SurfaceElevated
    }
    val border = when {
        !enabled   -> BorderGray.copy(alpha = 0.3f)
        isSelected -> PrimaryDark
        else       -> BorderGray
    }
    val text = when {
        !enabled   -> TextSecondary.copy(alpha = 0.4f)
        isSelected -> Color.White
        else       -> TextPrimary
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bg)
                .border(0.5.dp, border, RoundedCornerShape(20.dp))
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 14.sp, color = if (!enabled) text else Color.Unspecified)
            Spacer(Modifier.width(6.dp))
            Text(
                text       = label,
                fontSize   = 13.sp,
                color      = text,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        if (badge != null) {
            Text(
                text       = badge,
                fontSize   = 10.sp,
                color      = if (isSelected) PrimaryDark else TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.padding(top = 2.dp)
            )
        } else if (!enabled) {
            Text(
                text     = stringResource(Res.string.portfolio_add_tx_no_units_badge),
                fontSize = 10.sp,
                color    = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
internal fun EmptyAssetsHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(Res.string.portfolio_add_tx_no_assets_hint), fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
internal fun EmptyPlatformsInlineHint() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .padding(14.dp)
    ) {
        Text(text = stringResource(Res.string.portfolio_add_tx_no_platforms_hint), fontSize = 12.sp, color = TextSecondary)
    }
}
