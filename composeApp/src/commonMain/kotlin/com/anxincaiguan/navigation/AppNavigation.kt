package com.anxincaiguan.navigation

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.anxincaiguan.data.local.DatabaseDriverFactory
import com.anxincaiguan.data.local.Repository

@Composable
fun AppNavigation(driverFactory: DatabaseDriverFactory) {
    val driver = remember { driverFactory.createDriver() }
    val repository = remember { Repository(driver) }
    var onboarded by remember { mutableStateOf(false) }
    var ready by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onboarded = repository.isOnboarded()
        ready = true
    }

    if (ready) {
        val initialScreen: Screen = if (onboarded) HomePage(repository) else OnboardingPage(repository)
        Navigator(initialScreen) { navigator ->
            SlideTransition(navigator)
        }
    }
}
