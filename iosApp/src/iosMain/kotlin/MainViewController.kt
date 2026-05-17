import UIKit
import androidx.compose.ui.window.ComposeUIViewController
import com.anxincaiguan.App

class MainViewController: UIViewController() {
    override func loadView() {
        view = UIView()
        view.backgroundColor = .white
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        
        let composeViewController = ComposeUIViewController {
            App()
        }
        
        addChild(composeViewController)
        composeViewController.view.frame = view.bounds
        composeViewController.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(composeViewController.view)
        composeViewController.didMove(toParent: self)
    }
}