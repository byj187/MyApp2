package com.anxincaiguan.ui.report

import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import com.anxincaiguan.ui.theme.fmt0
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimeFilter(val label: String) {
    THIS_MONTH("本月"),
    LAST_MONTH("上月"),
    LAST_3_MONTHS("近3月"),
    THIS_YEAR("本年"),
    CUSTOM("自定义")
}

data class ReportDetailItem(
    val id: Long,
    val type: String,
    val amount: Double,
    val date: String,
    val description: String,
    val note: String = ""
)

data class MonthlyAggregate(
    val month: String,
    val income: Double,
    val expense: Double
)

data class ReportUiState(
    val timeFilter: TimeFilter = TimeFilter.THIS_MONTH,
    val mainAccounts: List<MainAccount> = emptyList(),
    val growthSubAccounts: List<GrowthSubAccount> = emptyList(),
    val totalGrowthAmount: Double = 0.0,
    val details: List<ReportDetailItem> = emptyList(),
    val monthlyAggregates: List<MonthlyAggregate> = emptyList(),
    val subAccountIncomes: List<Pair<String, Double>> = emptyList(),
    val cumulativeIncome: Double = 0.0,
    val isLoading: Boolean = false
)

class ReportViewModel(private val repository: Repository) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun setFilter(filter: TimeFilter) {
        _uiState.value = _uiState.value.copy(timeFilter = filter)
        loadData()
    }

    fun loadData() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val filter = _uiState.value.timeFilter
                val (startDate, endDate) = getDateRange(filter)

                val mains = repository.getAllMainAccounts()
                val growthSubs = repository.getAllGrowthSubAccounts()
                val totalGrowth = growthSubs.sumOf { it.idleAmount + it.investedAmount }

                val allBills = repository.getAllBills().filter { it.date >= startDate && it.date <= endDate }
                val allTransfers = repository.getAllTransfers().filter { it.date >= startDate && it.date <= endDate }
                val allSalaries = repository.getAllSalaryRecords().filter { it.date >= startDate && it.date <= endDate }
                val allIncomes = repository.getAllInvestmentIncomes().filter { it.date >= startDate && it.date <= endDate }

                val details = mutableListOf<ReportDetailItem>()
                details.addAll(allBills.map {
                    ReportDetailItem(it.id, if (it.type == "EXPENSE") "支出" else "收入", if (it.type == "EXPENSE") -it.amount else it.amount, it.date, "${it.category}", it.note)
                })
                details.addAll(allTransfers.map {
                    ReportDetailItem(it.id + 10000, "调拨", -it.amount, it.date, "${it.fromType}->${it.toType}", it.note)
                })
                details.addAll(allSalaries.map {
                    ReportDetailItem(it.id + 20000, "工资", it.total, it.date, "工资分配", "固定¥${fmt0(it.dailyFixed)}")
                })
                details.addAll(allIncomes.map {
                    ReportDetailItem(it.id + 30000, "收益", it.amount, it.date, it.note.ifEmpty { "投资收益" }, it.incomeType)
                })
                val sortedDetails = details.sortedByDescending { it.date + String.format("%010d", it.id) }

                val monthlyAggs = aggregateMonthly(allBills, startDate, endDate)

                val subIncomes = growthSubs.map { sub ->
                    val subType = GrowthSubAccountType.fromName(sub.name)
                    val subIncome = allIncomes.filter { it.productId == sub.id }.sumOf { it.amount }
                    subType.displayName to subIncome
                }
                val cumulativeInc = allIncomes.sumOf { it.amount }

                _uiState.value = ReportUiState(
                    timeFilter = filter,
                    mainAccounts = mains,
                    growthSubAccounts = growthSubs,
                    totalGrowthAmount = totalGrowth,
                    details = sortedDetails,
                    monthlyAggregates = monthlyAggs,
                    subAccountIncomes = subIncomes,
                    cumulativeIncome = cumulativeInc,
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun getDateRange(filter: TimeFilter): Pair<String, String> {
        val year = 2026
        return when (filter) {
            TimeFilter.THIS_MONTH -> "2026-05-01" to "2026-05-31"
            TimeFilter.LAST_MONTH -> "2026-04-01" to "2026-04-30"
            TimeFilter.LAST_3_MONTHS -> "2026-03-01" to "2026-05-31"
            TimeFilter.THIS_YEAR -> "2026-01-01" to "2026-12-31"
            TimeFilter.CUSTOM -> "2026-01-01" to "2026-12-31"
        }
    }

    private fun aggregateMonthly(bills: List<Bill>, startDate: String, endDate: String): List<MonthlyAggregate> {
        val months = mutableMapOf<String, MutableList<Double>>()
        bills.forEach { bill ->
            val month = bill.date.take(7)
            val agg = months.getOrPut(month) { mutableListOf(0.0, 0.0) }
            if (bill.type == "INCOME") agg[0] += bill.amount else agg[1] += bill.amount
        }
        return months.entries.sortedBy { it.key }.map { (m, v) ->
            MonthlyAggregate(m, v[0], v[1])
        }
    }
}
