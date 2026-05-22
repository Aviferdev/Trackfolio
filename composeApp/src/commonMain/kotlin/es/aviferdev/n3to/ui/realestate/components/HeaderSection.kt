package es.aviferdev.n3to.ui.realestate.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.ui.common.StatusTag
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.realestate_own_use_badge
import n3to.composeapp.generated.resources.realestate_rented_badge
import n3to.composeapp.generated.resources.realestate_sold_badge
import n3to.composeapp.generated.resources.realestate_vacant_badge
import org.jetbrains.compose.resources.stringResource

@Composable
fun HeaderSection(property: RealEstateProperty) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(property.propertyType.emoji, fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        property.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    Text(
                        property.address,
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.textTertiary
                    )
                }
                StatusTag(
                    label = when {
                        property.isSold -> "\u2705 ${stringResource(Res.string.realestate_sold_badge)}"
                        property.rentalStatus == RentalStatus.RENTED -> "\uD83D\uDCB0 ${
                            stringResource(Res.string.realestate_rented_badge)
                        }"
                        property.rentalStatus == RentalStatus.VACANT -> "\uD83D\uDD12 ${
                            stringResource(Res.string.realestate_vacant_badge)
                        }"
                        else -> "\uD83C\uDFE0 ${stringResource(Res.string.realestate_own_use_badge)}"
                    },
                    color = when {
                        property.isSold -> MaterialTheme.appColors.income
                        property.rentalStatus == RentalStatus.RENTED -> MaterialTheme.appColors.income
                        property.rentalStatus == RentalStatus.VACANT -> MaterialTheme.appColors.warnAmber
                        else -> MaterialTheme.appColors.textTertiary
                    }
                )
            }
        }
    }
}
