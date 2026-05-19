package es.aviferdev.n3to.ui.fixedincome.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.portfolio.ScheduledCoupon
import es.aviferdev.n3to.ui.theme.*
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.fixedincome_coupon_calendar
import n3to.composeapp.generated.resources.fixedincome_coupon_paid
import n3to.composeapp.generated.resources.fixedincome_coupon_pending
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CouponTimelineSection(
    schedule: List<ScheduledCoupon>,
    balancesHidden: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.fixedincome_coupon_calendar),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            schedule.forEachIndexed { index, coupon ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (coupon.isPaid) PositiveGreen else CyanAccent,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(text = formatDate(coupon.date), fontSize = 13.sp, color = Color.White)
                            Text(
                                text = if (coupon.isPaid)
                                    "✅ ${stringResource(Res.string.fixedincome_coupon_paid)}"
                                else
                                    "🔵 ${stringResource(Res.string.fixedincome_coupon_pending)}",
                                fontSize = 11.sp,
                                color = if (coupon.isPaid) PositiveGreen.copy(alpha = 0.8f) else CyanAccent.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Text(
                        text = "${maskAmount(formatAmount(coupon.grossAmount), balancesHidden)} €",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                if (index < schedule.lastIndex) {
                    HorizontalDivider(color = NavyBorder, modifier = Modifier.padding(start = 20.dp))
                }
            }
        }
    }
}
