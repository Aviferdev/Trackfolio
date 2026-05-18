package es.aviferdev.n3to.ui.valuable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValuableListScreen(
    onValuableClick: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: ValuableListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bienes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundGray,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BrandGreen
                )
            } else if (uiState.valuables.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.Sell,
                    title = "No hay bienes",
                    subtitle = "Añade tu primer bien para empezar a seguir su valor",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bienes activos
                    val active = uiState.valuables.filter { !it.isSold }
                    val sold = uiState.valuables.filter { it.isSold }

                    if (active.isNotEmpty()) {
                        item {
                            Text(
                                "En stock (${active.size})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(active, key = { it.id }) { valuable ->
                            ValuableCard(
                                valuable = valuable,
                                onClick = { onValuableClick(valuable.id) }
                            )
                        }
                    }

                    if (sold.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Vendidos (${sold.size})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(sold, key = { it.id }) { valuable ->
                            ValuableCard(
                                valuable = valuable,
                                onClick = { onValuableClick(valuable.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
