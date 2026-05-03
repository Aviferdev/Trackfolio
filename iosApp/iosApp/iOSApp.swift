import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        AppInitializer.shared.start()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
