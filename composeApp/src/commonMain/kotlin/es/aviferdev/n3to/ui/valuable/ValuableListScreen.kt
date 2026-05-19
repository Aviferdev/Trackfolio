package es.aviferdev.n3to.ui.valuable

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_back_cd
import n3to.composeapp.generated.resources.valuable_empty_subtitle
import n3to.composeapp.generated.resources.valuable_empty_title
import n3to.composeapp.generated.resources.valuable_list_title

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
                title = { Text(stringResource(Res.string.valuable_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back_cd))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.appColors.background,
                    titleContentColor = MaterialTheme.appColors.textPrimary
                )
            )
        },
        containerColor = MaterialTheme.appColors.background
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.appColors.income
                )
            } else if (uiState.valuables.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.Sell,
                    title = stringResource(Res.string.valuable_empty_title),
                    subtitle = stringResource(Res.string.valuable_empty_subtitle),
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
                                color = MaterialTheme.appColors.textSecondary,
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
                                color = MaterialTheme.appColors.textSecondary,
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
