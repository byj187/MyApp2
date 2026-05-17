package com.anxincaiguan.navigation

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.ui.account.AccountDetailScreen
import com.anxincaiguan.ui.account.TransferScreen
import com.anxincaiguan.ui.growth.GrowthMainScreen
import com.anxincaiguan.ui.growth.GrowthSubDetailScreen
import com.anxincaiguan.ui.home.HomeScreen
import com.anxincaiguan.ui.onboarding.OnboardingScreen
import com.anxincaiguan.ui.rebalance.RebalanceScreen
import com.anxincaiguan.ui.report.ReportScreen
import com.anxincaiguan.ui.profile.MineScreen
import com.anxincaiguan.ui.stable.StableDetailScreen
import kotlinx.coroutines.launch

class OnboardingPage(private val repository: Repository) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        OnboardingScreen(
            onComplete = { mainAccounts, growthSubAccounts, settings ->
                scope.launch {
                    repository.saveMainAccounts(mainAccounts)
                    repository.saveGrowthSubAccounts(growthSubAccounts)
                    repository.saveSettings(settings)
                    navigator.replaceAll(HomePage(repository))
                }
            }
        )
    }
}

class HomePage(private val repository: Repository) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        HomeScreen(
            repository = repository,
            onNavigateToAccount = { accountId -> navigator.push(AccountDetailPage(repository, accountId)) },
            onNavigateToGrowth = { navigator.push(GrowthMainPage(repository)) },
            onNavigateToRebalance = { navigator.push(RebalancePage()) },
            onNavigateToTransfer = { navigator.push(TransferPage(repository)) }
        )
    }
}

class AccountDetailPage(private val repository: Repository, private val accountId: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        AccountDetailScreen(
            accountId = accountId,
            repository = repository,
            onBack = { navigator.pop() },
            onNavigateToStableDetail = { navigator.push(StableDetailPage(repository, accountId)) }
        )
    }
}

class GrowthMainPage(private val repository: Repository) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        GrowthMainScreen(
            repository = repository,
            onNavigateToSubAccount = { subAccountId -> navigator.push(GrowthSubDetailPage(repository, subAccountId)) },
            onNavigateToRebalance = { navigator.push(RebalancePage()) },
            onBack = { navigator.pop() }
        )
    }
}

class GrowthSubDetailPage(private val repository: Repository, private val subAccountId: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        GrowthSubDetailScreen(
            subAccountId = subAccountId,
            repository = repository,
            onBack = { navigator.pop() }
        )
    }
}

class TransferPage(private val repository: Repository) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TransferScreen(
            repository = repository,
            onBack = { navigator.pop() }
        )
    }
}

class RebalancePage : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        RebalanceScreen(
            onBack = { navigator.pop() }
        )
    }
}

class ReportPage(private val repository: Repository) : Screen {
    @Composable
    override fun Content() {
        ReportScreen(repository = repository, onBack = {})
    }
}

class MinePage(private val repository: Repository) : Screen {
    @Composable
    override fun Content() {
        MineScreen(repository = repository, onNavigateToOnboarding = {})
    }
}

class StableDetailPage(private val repository: Repository, private val accountId: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        StableDetailScreen(
            accountId = accountId,
            repository = repository,
            onBack = { navigator.pop() }
        )
    }
}
