package com.anxincaiguan

import androidx.compose.ui.window.ComposeUIViewController
import com.anxincaiguan.data.local.DatabaseDriverFactory

fun MainViewController() =
    ComposeUIViewController {
        // Create a simple driver factory for testing
        val driverFactory = DatabaseDriverFactory()
        App(driverFactory = driverFactory)
    }