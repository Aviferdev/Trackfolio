package es.aviferdev.n3to.ui.common.help

import es.aviferdev.n3to.core.security.AppSettings

class HelpPreferences(private val settings: AppSettings) {

    fun isDismissed(key: String): Boolean = settings.getBool("help_$key", false)

    fun dismiss(key: String) {
        settings.putBool("help_$key", true)
    }
}
