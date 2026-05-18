package es.aviferdev.n3to.ui.portfolio

import es.aviferdev.n3to.platform.nowMillis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.repository.AssetPlatformRepository
import es.aviferdev.n3to.domain.repository.AssetRepository
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.n3to.domain.usecase.platform.SavePlatformUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AssetDetailUiState(
    val asset: Asset?                    = null,
    val allPlatforms: List<Platform>     = emptyList(),
    val linkedPlatformIds: Set<String>   = emptySet(),
    val linkedPlatforms: List<Platform>  = emptyList(),
    val showAddPlatformSheet: Boolean    = false,
    val error: String?                   = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class AssetDetailViewModel(
    private val assetId: String,
    private val assetRepository: AssetRepository,
    private val assetPlatformRepository: AssetPlatformRepository,
    private val getPlatforms: GetPlatformsUseCase,
    private val savePlatform: SavePlatformUseCase
) : ViewModel() {

    private val _showAddPlatformSheet = MutableStateFlow(false)
    private val _error                = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AssetDetailUiState> = combine(
        assetRepository.getAssetById(assetId),
        getPlatforms(),
        assetPlatformRepository.getPlatformsByAsset(assetId),
        _showAddPlatformSheet,
        _error
    ) { asset, allPlatforms, linkedPlatforms, showSheet, error ->
        val linkedIds = linkedPlatforms.map { it.id }.toSet()
        AssetDetailUiState(
            asset                = asset,
            allPlatforms         = allPlatforms,
            linkedPlatformIds    = linkedIds,
            linkedPlatforms      = linkedPlatforms,
            showAddPlatformSheet = showSheet,
            error                = error
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = AssetDetailUiState()
    )

    fun linkPlatform(platformId: String) {
        viewModelScope.launch {
            assetPlatformRepository.link(assetId, platformId)
                .onFailure { _error.value = it.message }
        }
    }

    fun unlinkPlatform(platformId: String) {
        viewModelScope.launch {
            assetPlatformRepository.unlink(assetId, platformId)
                .onFailure { _error.value = it.message }
        }
    }

    fun openAddPlatformSheet()  { _showAddPlatformSheet.value = true }
    fun closeAddPlatformSheet() { _showAddPlatformSheet.value = false }

    /**
     * Crea una nueva plataforma global y la vincula automáticamente al activo.
     */
    fun createAndLinkPlatform(name: String, icon: String, notes: String?) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val validatedNotes = notes?.take(200)?.ifBlank { null }
        viewModelScope.launch {
            val now = nowMillis()
            val allPlats = uiState.value.allPlatforms
            val nextOrder = (allPlats.maxOfOrNull { it.sortOrder } ?: -1) + 1
            val platform = Platform(
                id        = "platform_$now",
                name      = trimmed,
                icon      = icon.ifBlank { "🏦" },
                sortOrder = nextOrder,
                createdAt = now,
                notes     = validatedNotes
            )
            savePlatform(platform)
                .onSuccess {
                    assetPlatformRepository.link(assetId, platform.id)
                }
                .onFailure { _error.value = it.message }
            _showAddPlatformSheet.value = false
        }
    }

    fun clearError() { _error.value = null }
}
