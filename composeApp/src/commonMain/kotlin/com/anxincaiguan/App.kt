package com.anxincaiguan

import androidx.compose.runtime.Composable
import com.anxincaiguan.data.local.DatabaseDriverFactory
import com.anxincaiguan.navigation.AppNavigation
import com.anxincaiguan.ui.theme.AnxinCaiGuanTheme

@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    AnxinCaiGuanTheme {
        AppNavigation(driverFactory = driverFactory)
    }
}
