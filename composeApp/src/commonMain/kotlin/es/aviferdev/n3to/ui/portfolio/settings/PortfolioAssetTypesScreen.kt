package es.aviferdev.n3to.ui.portfolio.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.toMaterialIcon
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.usecase.asset.GetAssetsByAccountUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.portfolio.settings.components.SettingsGroupCard
import es.aviferdev.n3to.ui.theme.appColors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_settings_assets_available
import n3to.composeapp.generated.resources.portfolio_settings_category_count_many
import n3to.composeapp.generated.resources.portfolio_settings_category_count_one
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

data class PortfolioAssetTypesUiState(
    val categories: List<AssetCategory> = emptyList(),
    val assets: List<Asset> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioAssetTypesViewModel(
    session: AccountSession,
    getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    getAssetsByAccount: GetAssetsByAccountUseCase
) : ViewModel() {

    val uiState: StateFlow<PortfolioAssetTypesUiState> = combine(
        getAssetCategoriesIncludingArchived().map { list -> list.filter { !it.archived } },
        session.selectedAccountId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else getAssetsByAccount(id)
        }
    ) { categories, assets ->
        PortfolioAssetTypesUiState(categories = categories, assets = assets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PortfolioAssetTypesUiState())
}

@Composable
fun PortfolioAssetTypesScreen(
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit,
    viewModel: PortfolioAssetTypesViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val assetsByCategory = state.assets.groupBy { it.assetCategoryId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        TopBarWithActionsApp(
            title = stringResource(Res.string.portfolio_settings_assets_available),
            navigateBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
        ) {
            item {
                SettingsGroupCard {
                    if (state.categories.isEmpty()) {
                        Text(
                            "Sin categorías configuradas",
                            fontSize = 13.sp,
                            color = MaterialTheme.appColors.textSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                        )
                    } else {
                        state.categories.forEachIndexed { index, category ->
                            val count = assetsByCategory[category.id]?.size ?: 0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCategoryClick(category.id) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = category.icon.toMaterialIcon(),
                                    contentDescription = null,
                                    tint = MaterialTheme.appColors.cyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = category.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.appColors.textPrimary
                                    )
                                    Text(
                                        text = if (count == 1)
                                            stringResource(Res.string.portfolio_settings_category_count_one, count)
                                        else
                                            stringResource(Res.string.portfolio_settings_category_count_many, count),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.appColors.textSecondary
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.appColors.textTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (index < state.categories.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.appColors.border,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 56.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
