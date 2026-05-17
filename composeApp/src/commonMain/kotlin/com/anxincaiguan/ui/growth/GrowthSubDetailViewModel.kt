package com.anxincaiguan.ui.growth

import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GrowthSubDetailUiState(
    val subAccount: GrowthSubAccount? = null,
    val products: List<InvestmentProduct> = emptyList(),
    val total: Double = 0.0,
    val isLoading: Boolean = false
)

class GrowthSubDetailViewModel(
    private val repository: Repository,
    val subAccountId: Long
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _uiState = MutableStateFlow(GrowthSubDetailUiState())
    val uiState: StateFlow<GrowthSubDetailUiState> = _uiState.asStateFlow()

    fun loadData() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val sub = repository.getGrowthSubAccountById(subAccountId)
                val prods = repository.getProductsBySubAccount(subAccountId)
                    .filter { it.status == "ACTIVE" }
                val total = (sub?.idleAmount ?: 0.0) + (sub?.investedAmount ?: 0.0)
                _uiState.value = GrowthSubDetailUiState(
                    subAccount = sub,
                    products = prods,
                    total = total,
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // Buy: reduce idle, increase invested, save product
    fun buyProduct(productName: String, amount: Double, rate: Double, purchaseDate: String, expireDate: String?, onSuccess: () -> Unit) {
        scope.launch {
            try {
                val sub = repository.getGrowthSubAccountById(subAccountId) ?: return@launch
                if (amount > sub.idleAmount) return@launch
                repository.updateGrowthSubAccountIdle(subAccountId, -amount)
                repository.updateGrowthSubAccountInvested(subAccountId, amount)
                repository.insertInvestmentProduct(
                    InvestmentProduct(
                        subAccountId = subAccountId,
                        productName = productName,
                        amount = amount,
                        rate = rate,
                        purchaseDate = purchaseDate,
                        expireDate = expireDate,
                        status = "ACTIVE"
                    )
                )
                loadData()
                onSuccess()
            } catch (_: Exception) {}
        }
    }

    // Redeem: reduce product + invested, increase idle
    fun redeemProduct(product: InvestmentProduct, redeemAmount: Double, capitalGain: Double, onSuccess: () -> Unit) {
        scope.launch {
            try {
                if (redeemAmount > product.amount) return@launch
                val newAmount = product.amount - redeemAmount
                if (newAmount <= 0.01) {
                    repository.updateProductStatus(product.id, "REDEEMED")
                    repository.updateProductAmount(product.id, 0.0)
                } else {
                    repository.updateProductAmount(product.id, newAmount)
                }
                repository.updateGrowthSubAccountInvested(subAccountId, -redeemAmount)
                repository.updateGrowthSubAccountIdle(subAccountId, redeemAmount)

                if (capitalGain > 0) {
                    repository.insertInvestmentIncome(
                        InvestmentIncome(productId = product.id, amount = capitalGain, incomeType = "资本利得", date = "2026-05-07", note = "赎回-${product.productName}")
                    )
                    repository.updateGrowthSubAccountIdle(subAccountId, capitalGain)
                }
                loadData()
                onSuccess()
            } catch (_: Exception) {}
        }
    }

    // Transfer: reduce current idle, increase target idle
    fun transferTo(targetSubId: Long, amount: Double, onSuccess: () -> Unit) {
        scope.launch {
            try {
                val sub = repository.getGrowthSubAccountById(subAccountId) ?: return@launch
                if (amount > sub.idleAmount) return@launch
                repository.updateGrowthSubAccountIdle(subAccountId, -amount)
                repository.updateGrowthSubAccountIdle(targetSubId, amount)
                repository.insertTransfer(
                    Transfer(fromType = "growth", fromId = subAccountId, toType = "growth", toId = targetSubId, amount = amount, date = "2026-05-07", note = "子账户调拨")
                )
                loadData()
                onSuccess()
            } catch (_: Exception) {}
        }
    }

    // Income: add idle, save income
    fun recordIncome(productId: Long, amount: Double, incomeType: String, onSuccess: () -> Unit) {
        scope.launch {
            try {
                repository.updateGrowthSubAccountIdle(subAccountId, amount)
                repository.insertInvestmentIncome(
                    InvestmentIncome(productId = productId, amount = amount, incomeType = incomeType, date = "2026-05-07", note = incomeType)
                )
                loadData()
                onSuccess()
            } catch (_: Exception) {}
        }
    }

    fun getAllSubAccounts(): List<GrowthSubAccount> {
        return emptyList() // loaded lazily in dialog
    }
}
