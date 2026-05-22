package es.aviferdev.n3to.ui.realestate.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatDateFullLocalized
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.realestate_sale_date_label
import n3to.composeapp.generated.resources.realestate_sold
import org.jetbrains.compose.resources.stringResource

@Composable
fun SaleInfoSection(property: RealEstateProperty) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.appColors.income.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("\u2705", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(Res.string.realestate_sold),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.appColors.income
                )
            }
            Spacer(Modifier.height(8.dp))
            val saleDateStr = property.saleDate?.let { formatDateFullLocalized(it) } ?: ""
            DataRow(stringResource(Res.string.realestate_sale_date_label), saleDateStr)
        }
    }
}
