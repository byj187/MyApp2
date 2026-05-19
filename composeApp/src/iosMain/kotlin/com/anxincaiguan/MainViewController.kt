package com.anxincaiguan

import androidx.compose.ui.window.ComposeUIViewController
import com.anxincaiguan.data.local.DatabaseDriverFactory
import platform.UIKit.UIViewController

@Suppress("unused")
object IosApp {
    fun createMainViewController(): UIViewController =
        ComposeUIViewController {
            val driverFactory = DatabaseDriverFactory()
            App(driverFactory = driverFactory)
        }
}