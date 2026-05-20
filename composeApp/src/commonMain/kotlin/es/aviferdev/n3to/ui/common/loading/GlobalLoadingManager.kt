package es.aviferdev.n3to.ui.common.loading

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GlobalLoadingManager {

    private val _isLoading = MutableStateFlow<String?>(null)
    val isLoading: StateFlow<String?> = _isLoading.asStateFlow()

    fun show(textLoading: String) {
        _isLoading.value = textLoading
    }

    fun hide() {
        _isLoading.value = null
    }

}
