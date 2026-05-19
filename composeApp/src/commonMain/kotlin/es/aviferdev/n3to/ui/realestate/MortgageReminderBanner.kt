package es.aviferdev.n3to.ui.realestate

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MortgageReminderBanner(
    propertyName: String,
    onAddMortgage: () -> Unit,
    onDismiss: () -> Unit
) {
    var dismissed by remember { mutableStateOf(false) }

    if (!dismissed) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = es.aviferdev.n3to.ui.theme.PrimaryAlpha),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.HomeWork, null, tint = es.aviferdev.n3to.ui.theme.PrimaryDark, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Vincula una hipoteca a $propertyName",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Registra la hipoteca asociada para un cálculo preciso de tu patrimonio neto.",
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.textSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onAddMortgage,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = es.aviferdev.n3to.ui.theme.PrimaryDark)
                        ) { Text("Añadir hipoteca", fontSize = 11.sp) }
                        TextButton(onClick = {
                            dismissed = true
                            onDismiss()
                        }) { Text("Descartar", color = MaterialTheme.appColors.textTertiary, fontSize = 11.sp) }
                    }
                }
            }
        }
    }
}
