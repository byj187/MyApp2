package com.anxincaiguan.ui.growth

import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubAccountUi(
    val account: GrowthSubAccount,
    val total: Double,
    val actualRatio: Double,
    val targetRatio: Double,
    val deviation: Double,
    val isDeviated: Boolean
)

data class GrowthMainUiState(
    val growthAccountBalance: Double = 0.0,
    val cumulativeIncome: Double = 0.0,
    val subAccounts: List<SubAccountUi> = emptyList(),
    val isLoading: Boolean = false
)

class GrowthMainViewModel(private val repository: Repository) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _uiState = MutableStateFlow(GrowthMainUiState())
    val uiState: StateFlow<GrowthMainUiState> = _uiState.asStateFlow()

    fun loadData() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val mains = repository.getAllMainAccounts()
                val growthAccount = mains.find { it.name == MainAccountType.GROWTH.displayName }
                val growthBalance = growthAccount?.balance ?: 0.0

                val incomes = repository.getAllInvestmentIncomes()
                val cumulativeIncome = incomes.sumOf { it.amount }

                val settings = repository.getSettings()
                val threshold = settings?.deviationThreshold ?: 0.05

                val subs = repository.getAllGrowthSubAccounts()
                val totalGrowth = subs.sumOf { it.idleAmount + it.investedAmount }

                val subAccounts = subs.map { sub ->
                    val total = sub.idleAmount + sub.investedAmount
                    val actualRatio = if (totalGrowth > 0) total / totalGrowth else 0.0
                    val deviation = actualRatio - sub.targetRatio
                    SubAccountUi(
                        account = sub,
                        total = total,
                        actualRatio = actualRatio,
                        targetRatio = sub.targetRatio,
                        deviation = deviation,
                        isDeviated = kotlin.math.abs(deviation) > threshold
                    )
                }

                _uiState.value = GrowthMainUiState(
                    growthAccountBalance = growthBalance,
                    cumulativeIncome = cumulativeIncome,
                    subAccounts = subAccounts,
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
