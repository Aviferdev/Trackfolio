package es.aviferdev.n3to

import androidx.compose.ui.window.ComposeUIViewController
import cocoapods.FirebaseCore.FIRApp
import es.aviferdev.n3to.di.initKoinIos
import kotlinx.cinterop.ExperimentalForeignApi

fun MainViewController() = ComposeUIViewController {
    App()
}

object AppInitializer {
    @OptIn(ExperimentalForeignApi::class)
    fun start() {
        FIRApp.configure()
        initKoinIos()
    }
}
