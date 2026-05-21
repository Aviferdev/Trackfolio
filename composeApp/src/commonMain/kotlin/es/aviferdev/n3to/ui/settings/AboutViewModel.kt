package es.aviferdev.n3to.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.core.premium.PremiumManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AboutViewModel(
    premiumManager: PremiumManager
) : ViewModel() {
    val appUserId: StateFlow<String> = premiumManager.status
        .map { it.appUserId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
}
