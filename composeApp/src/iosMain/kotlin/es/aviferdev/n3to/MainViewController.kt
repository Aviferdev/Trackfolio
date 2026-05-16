package es.aviferdev.n3to

import androidx.compose.ui.window.ComposeUIViewController
import es.aviferdev.n3to.di.initKoinIos

fun MainViewController() = ComposeUIViewController {
    App()
}

object AppInitializer {
    fun start() {
        initKoinIos()
    }
}
