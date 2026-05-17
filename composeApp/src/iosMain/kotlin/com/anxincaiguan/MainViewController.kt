package com.anxincaiguan

import androidx.compose.ui.window.ComposeUIViewController
import com.anxincaiguan.data.local.DatabaseDriverFactory

fun MainViewController(driverFactory: DatabaseDriverFactory) =
    ComposeUIViewController {
        App(driverFactory = driverFactory)
    }