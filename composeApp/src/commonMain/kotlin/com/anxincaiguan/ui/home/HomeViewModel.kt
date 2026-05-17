package com.anxincaiguan.ui.home

import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import com.anxincaiguan.ui.theme.fmt0
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val totalAssets: Double = 0.0,
    val mainAccounts: List<MainAccount> = emptyList(),
    val growthSubAccounts: List<GrowthSubAccount> = emptyList(),
    val deviationThreshold: Double = 0.05,
    val isRebalanceNeeded: Boolean = false,
    val dynamicItems: List<DynamicItem> = emptyList(),
    val isLoading: Boolean = false
)

data class DynamicItem(
    val id: Long,
    val type: String,
    val title: String,
    val amount: Double,
    val date: String,
    val note: String
)

class HomeViewModel(private val repository: Repository) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadData() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val mains = repository.getAllMainAccounts()
                val growthSubs = repository.getAllGrowthSubAccounts()
                val settings = repository.getSettings()
                val threshold = settings?.deviationThreshold ?: 0.05

                val totalAssets = mains.sumOf { it.balance }

                val totalGrowth = growthSubs.sumOf { it.idleAmount + it.investedAmount }
                val isRebalanceNeeded = if (totalGrowth > 0) {
                    growthSubs.any { sub ->
                        val actualRatio = (sub.idleAmount + sub.investedAmount) / totalGrowth
                        kotlin.math.abs(actualRatio - sub.targetRatio) > threshold
                    }
                } else false

                val bills = repository.getAllBills().map { bill ->
                    DynamicItem(
                        id = bill.id,
                        type = if (bill.type == "EXPENSE") "支出" else "收入",
                        title = "${if (bill.type == "EXPENSE") "支出" else "收入"}-${bill.category}",
                        amount = if (bill.type == "EXPENSE") -bill.amount else bill.amount,
                        date = bill.date,
                        note = bill.note
                    )
                }

                val transfers = repository.getAllTransfers().map { transfer ->
                    DynamicItem(
                        id = transfer.id + 10000,
                        type = "调拨",
                        title = "调拨-${transfer.fromType}->${transfer.toType}",
                        amount = -transfer.amount,
                        date = transfer.date,
                        note = transfer.note
                    )
                }

                val salaries = repository.getAllSalaryRecords().map { salary ->
                    DynamicItem(
                        id = salary.id + 20000,
                        type = "工资",
                        title = "工资分配",
                        amount = salary.total,
                        date = salary.date,
                        note = "固定¥${fmt0(salary.dailyFixed)}+分配"
                    )
                }

                val incomes = repository.getAllInvestmentIncomes().map { income ->
                    DynamicItem(
                        id = income.id + 30000,
                        type = "收益",
                        title = income.note.ifEmpty { "投资收益" },
                        amount = income.amount,
                        date = income.date,
                        note = income.incomeType
                    )
                }

                val allItems = (bills + transfers + salaries + incomes)
                    .sortedByDescending { it.date + String.format("%010d", it.id) }
                    .take(10)

                _uiState.value = HomeUiState(
                    totalAssets = totalAssets,
                    mainAccounts = mains,
                    growthSubAccounts = growthSubs,
                    deviationThreshold = threshold,
                    isRebalanceNeeded = isRebalanceNeeded,
                    dynamicItems = allItems,
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
