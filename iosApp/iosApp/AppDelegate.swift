import UIKit
import ComposeApp

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        window = UIWindow(frame: UIScreen.main.bounds)
        
        let driverFactory = DatabaseDriverFactory()
        let viewController = Main_iosKt.MainViewController(driverFactory: driverFactory)
        
        window?.rootViewController = viewController
        window?.makeKeyAndVisible()
        return true
    }
}
