package es.aviferdev.n3to.ui.inflation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.component.BetaBadge
import es.aviferdev.n3to.ui.common.component.NavyTabRow
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.home.InflationCountryChipRow
import es.aviferdev.n3to.ui.home.InflationHistoryTab
import es.aviferdev.n3to.ui.home.InflationSummaryTab
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.inflation_card_subtitle
import n3to.composeapp.generated.resources.inflation_card_title
import n3to.composeapp.generated.resources.inflation_screen_title
import n3to.composeapp.generated.resources.inflation_tab_history
import n3to.composeapp.generated.resources.inflation_tab_summary
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InflationScreen(
    onBack: () -> Unit,
    viewModel: InflationViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(Res.string.inflation_tab_summary),
        stringResource(Res.string.inflation_tab_history)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            TopBarWithActionsApp(
                title = stringResource(Res.string.inflation_screen_title),
                navigateBack = onBack,
                containerColor = MaterialTheme.appColors.navyDeep,
                dividerColor = MaterialTheme.appColors.navyBorder,
                actions = {
                    BetaBadge(modifier = Modifier.padding(end = 8.dp))
                }
            )

            // Fixed header: title + chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ShowChart,
                            contentDescription = null,
                            tint = MaterialTheme.appColors.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.inflation_card_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.appColors.textPrimary
                        )
                    }
                    Text(
                        text = stringResource(Res.string.inflation_card_subtitle),
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.textTertiary
                    )
                }

                Spacer(Modifier.height(12.dp))

                InflationCountryChipRow(
                    selectedCountries = state.selectedCountries,
                    onToggleCountry = { viewModel.toggleCountry(it) }
                )

                Spacer(Modifier.height(8.dp))
            }

            // Tab row
            NavyTabRow(
                items = tabs.indices.toList(),
                selected = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                content = { idx ->
                    Text(
                        text = tabs[idx],
                        fontSize = 12.sp,
                        fontWeight = if (idx == selectedTab) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (idx == selectedTab) MaterialTheme.appColors.textPrimary
                        else MaterialTheme.appColors.textSecondary
                    )
                }
            )

            HorizontalDivider(
                color = MaterialTheme.appColors.navyBorder,
                thickness = 1.dp
            )

            // Scrollable tab content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> InflationSummaryTab(
                        isLoading = state.isLoading,
                        data = state.data,
                        selectedCountries = state.selectedCountries
                    )
                    1 -> InflationHistoryTab(
                        isLoading = state.isLoading,
                        data = state.data,
                        selectedCountries = state.selectedCountries
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
