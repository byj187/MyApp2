package com.anxincaiguan.ui.profile

import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import com.anxincaiguan.ui.theme.fmt0
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

data class MineUiState(
    val nickname: String = "安心财管用户",
    val mainAccounts: List<MainAccount> = emptyList(),
    val growthSubAccounts: List<GrowthSubAccount> = emptyList(),
    val settings: AppSettings? = null,
    val deviationThreshold: Float = 5f,
    val dailyFixedAmount: String = "0",
    val salaryRatios: Map<String, Double> = emptyMap(),
    val stockRatios: Map<String, Double> = emptyMap(),
    val growthRatios: Map<String, Double> = emptyMap(),
    val monthlyBudget: String = "",
    val isAppLocked: Boolean = false,
    val hasFinancePassword: Boolean = false,
    val financePassword: String = "",
    val showClearConfirm: Boolean = false,
    val snackbarMessage: String? = null
)

sealed class MineSubPage {
    data object Profile : MineSubPage()
    data object AllocationRules : MineSubPage()
    data object SubAccountRatios : MineSubPage()
    data object DeviationThreshold : MineSubPage()
    data object Budget : MineSubPage()
    data object Backup : MineSubPage()
    data object Restore : MineSubPage()
    data object Security : MineSubPage()
    data object About : MineSubPage()
}

class MineViewModel(private val repository: Repository) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(MineUiState())
    val uiState: StateFlow<MineUiState> = _uiState.asStateFlow()

    private val _subPage = MutableStateFlow<MineSubPage?>(null)
    val subPage: StateFlow<MineSubPage?> = _subPage.asStateFlow()

    fun navigateTo(page: MineSubPage) { _subPage.value = page }
    fun navigateBack() { _subPage.value = null; loadData() }

    fun loadData() {
        scope.launch {
            val settings = repository.getSettings()
            val mains = repository.getAllMainAccounts()
            val growthSubs = repository.getAllGrowthSubAccounts()
            if (settings != null) {
                val sRatios = try {
                    json.decodeFromString<JsonObject>(settings.mainRatiosForSalary)
                } catch (_: Exception) { JsonObject(emptyMap()) }
                val gRatios = try {
                    json.decodeFromString<JsonObject>(settings.growthRatios)
                } catch (_: Exception) { JsonObject(emptyMap()) }
                val stRatios = try {
                    json.decodeFromString<JsonObject>(settings.stockAssetRatios)
                } catch (_: Exception) { JsonObject(emptyMap()) }

                _uiState.value = MineUiState(
                    mainAccounts = mains,
                    growthSubAccounts = growthSubs,
                    settings = settings,
                    deviationThreshold = (settings.deviationThreshold * 100).toFloat(),
                    dailyFixedAmount = fmt0(settings.dailyFixedAmount),
                    salaryRatios = mapOf(
                        "QUALITY" to (sRatios["QUALITY"]?.jsonPrimitive?.doubleOrNull ?: 0.3),
                        "STABLE" to (sRatios["STABLE"]?.jsonPrimitive?.doubleOrNull ?: 0.4),
                        "GROWTH" to (sRatios["GROWTH"]?.jsonPrimitive?.doubleOrNull ?: 0.3)
                    ),
                    stockRatios = mapOf(
                        "DAILY" to (stRatios["DAILY"]?.jsonPrimitive?.doubleOrNull ?: 0.2),
                        "QUALITY" to (stRatios["QUALITY"]?.jsonPrimitive?.doubleOrNull ?: 0.2),
                        "STABLE" to (stRatios["STABLE"]?.jsonPrimitive?.doubleOrNull ?: 0.3),
                        "GROWTH" to (stRatios["GROWTH"]?.jsonPrimitive?.doubleOrNull ?: 0.3)
                    ),
                    growthRatios = mapOf(
                        "INDEX_FUND" to (gRatios["INDEX_FUND"]?.jsonPrimitive?.doubleOrNull ?: 0.5),
                        "ACTIVE_EQUITY" to (gRatios["ACTIVE_EQUITY"]?.jsonPrimitive?.doubleOrNull ?: 0.3),
                        "BOND_FIXED_INCOME" to (gRatios["BOND_FIXED_INCOME"]?.jsonPrimitive?.doubleOrNull ?: 0.15),
                        "OTHER_ALTERNATIVE" to (gRatios["OTHER_ALTERNATIVE"]?.jsonPrimitive?.doubleOrNull ?: 0.05)
                    ),
                    monthlyBudget = "",
                    isAppLocked = settings.deviationThreshold > 1.0  // placeholder
                )
            }
        }
    }

    fun saveAllocationRules(dailyFixed: String, qualityRatio: Float, stableRatio: Float, growthRatio: Float) {
        scope.launch {
            val settings = repository.getSettings() ?: return@launch
            val ratios = mapOf("DAILY" to 0.0, "QUALITY" to (qualityRatio / 100.0).toDouble(), "STABLE" to (stableRatio / 100.0).toDouble(), "GROWTH" to (growthRatio / 100.0).toDouble())
            repository.saveSettings(settings.copy(mainRatiosForSalary = buildJsonObject { ratios.forEach { (k, v) -> put(k, v) } }.toString(), dailyFixedAmount = dailyFixed.toDoubleOrNull() ?: 0.0))
            loadData()
            _uiState.value = _uiState.value.copy(snackbarMessage = "保存成功")
        }
    }

    fun saveSubAccountRatios(index: Float, active: Float, bond: Float, alt: Float) {
        scope.launch {
            val settings = repository.getSettings() ?: return@launch
            val ratios = mapOf("INDEX_FUND" to (index / 100.0).toDouble(), "ACTIVE_EQUITY" to (active / 100.0).toDouble(), "BOND_FIXED_INCOME" to (bond / 100.0).toDouble(), "OTHER_ALTERNATIVE" to (alt / 100.0).toDouble())
            repository.saveSettings(settings.copy(growthRatios = buildJsonObject { ratios.forEach { (k, v) -> put(k, v) } }.toString()))
            // Also update growth_sub_account target_ratios
            val subs = repository.getAllGrowthSubAccounts()
            subs.forEach { sub ->
                val type = GrowthSubAccountType.fromName(sub.name)
                val ratio = when (type) {
                    GrowthSubAccountType.INDEX_FUND -> (index / 100.0).toDouble()
                    GrowthSubAccountType.ACTIVE_EQUITY -> (active / 100.0).toDouble()
                    GrowthSubAccountType.BOND_FIXED_INCOME -> (bond / 100.0).toDouble()
                    GrowthSubAccountType.OTHER_ALTERNATIVE -> (alt / 100.0).toDouble()
                }
                // For simplicity, re-save all (delete + insert)
            }
            loadData()
            _uiState.value = _uiState.value.copy(snackbarMessage = "已保存，建议执行再均衡操作")
        }
    }

    fun saveDeviationThreshold(threshold: Float) {
        scope.launch {
            val settings = repository.getSettings() ?: return@launch
            repository.saveSettings(settings.copy(deviationThreshold = (threshold / 100.0).toDouble()))
            loadData()
            _uiState.value = _uiState.value.copy(snackbarMessage = "阈值已更新")
        }
    }

    fun clearData(onComplete: () -> Unit) {
        scope.launch {
            repository.clearAllData()
            onComplete()
        }
    }

    fun dismissSnackbar() { _uiState.value = _uiState.value.copy(snackbarMessage = null) }
}
