import SwiftUI
import KotlinLibrary

class AppDelegate: NSObject, UIApplicationDelegate {

    // Create the raw KMP instance (as you had it)
    static let firebaseKMPRawInstance: FirebaseKMP = FirebaseKMPFactory().firebase()
    // Create the ObservableObject wrapper for SwiftUI
    static let firebaseService = FirebaseService(kmpInstance: firebaseKMPRawInstance)

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {

        // Configure Firebase through the service
        // The service's configure() method calls the KMP instance's configure()
        AppDelegate.firebaseService.configure()

        print("Firebase KMP Configured via FirebaseService in AppDelegate.")
        return true
    }
}

@main
struct iOSApp: App {
    // register app delegate for Firebase setup
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView().environmentObject(AppDelegate.firebaseService)
        }
    }
}
