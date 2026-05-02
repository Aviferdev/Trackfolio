package es.aviferdev.trackfolio

import androidx.compose.ui.window.ComposeUIViewController
import es.aviferdev.trackfolio.di.initKoinIos

fun MainViewController() = ComposeUIViewController {
    App()
}

fun initApp() {
    initKoinIos()
}
