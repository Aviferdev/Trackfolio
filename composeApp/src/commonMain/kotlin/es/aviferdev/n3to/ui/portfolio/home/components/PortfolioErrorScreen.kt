package es.aviferdev.n3to.ui.portfolio.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.aviferdev.n3to.ui.theme.appColors

@Composable
fun PortfolioErrorScreen(message: String) {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = MaterialTheme.appColors.expense)
    }
}
