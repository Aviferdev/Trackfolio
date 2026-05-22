package es.aviferdev.n3to.ui.settings.emergencyfund.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.ef_onboarding_badge
import n3to.composeapp.generated.resources.ef_onboarding_subtitle
import n3to.composeapp.generated.resources.ef_onboarding_title
import n3to.composeapp.generated.resources.ef_recommendation_text
import n3to.composeapp.generated.resources.ef_recommendation_title
import n3to.composeapp.generated.resources.ef_what_is_text
import n3to.composeapp.generated.resources.ef_what_is_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmergencyFundHeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.appColors.navySurface,
                        MaterialTheme.appColors.navySurfaceLight
                    )
                )
            )
            .border(0.5.dp, MaterialTheme.appColors.navyBorder, RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.appColors.cyanGlow.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.appColors.cyanAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.cyanAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.ef_onboarding_badge),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.appColors.cyanAccent.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(Res.string.ef_onboarding_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(Res.string.ef_onboarding_subtitle),
                fontSize = 12.sp,
                color = MaterialTheme.appColors.textSecondary,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun EmergencyFundInfoCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.appColors.cyanAccent.copy(alpha = 0.06f))
            .border(
                0.5.dp,
                MaterialTheme.appColors.cyanAccent.copy(alpha = 0.25f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.appColors.cyanAccent,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = stringResource(Res.string.ef_what_is_title),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.appColors.cyanAccent
            )
        }
        Text(
            text = stringResource(Res.string.ef_what_is_text),
            fontSize = 12.sp,
            color = MaterialTheme.appColors.textSecondary,
            lineHeight = 17.sp
        )
        HorizontalDivider(
            color = MaterialTheme.appColors.cyanAccent.copy(alpha = 0.15f),
            thickness = 0.5.dp
        )
        Text(
            text = stringResource(Res.string.ef_recommendation_title),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = MaterialTheme.appColors.textTertiary
        )
        Text(
            text = stringResource(Res.string.ef_recommendation_text),
            fontSize = 12.sp,
            color = MaterialTheme.appColors.textSecondary,
            lineHeight = 18.sp
        )
    }
}
