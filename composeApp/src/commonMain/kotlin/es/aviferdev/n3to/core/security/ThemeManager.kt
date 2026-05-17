package es.aviferdev.n3to.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val KEY_THEME_DARK = "theme_dark"

class ThemeManager(private val settings: AppSettings) {
    private val _isDark = MutableStateFlow(settings.getBool(KEY_THEME_DARK, true))
    val isDark: StateFlow<Boolean> = _isDark.asStateFlow()

    fun set(dark: Boolean) {
        if (_isDark.value == dark) return
        _isDark.value = dark
        settings.putBool(KEY_THEME_DARK, dark)
    }
}
