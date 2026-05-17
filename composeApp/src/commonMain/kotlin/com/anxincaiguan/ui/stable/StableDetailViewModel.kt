package com.anxincaiguan.ui.stable

import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StableDetailUiState(
    val account: MainAccount? = null,
    val products: List<StableProduct> = emptyList(),
    val totalLocked: Double = 0.0,
    val totalIncome: Double = 0.0,
    val expiredProducts: List<StableProduct> = emptyList(),
    val isLoaded: Boolean = false
)

class StableDetailViewModel(
    private val repository: Repository,
    val accountId: Long
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _uiState = MutableStateFlow(StableDetailUiState())
    val uiState: StateFlow<StableDetailUiState> = _uiState.asStateFlow()

    fun loadData() {
        scope.launch {
            try {
                val account = repository.getMainAccountById(accountId)
                val allProducts = repository.getStableProductsByAccount(accountId)
                val activeProducts = allProducts.filter { it.status == "ACTIVE" }
                val expired = allProducts.filter { it.status == "ACTIVE" && it.expireDate != null && it.expireDate!! <= "2026-05-07" }

                val totalLocked = activeProducts.sumOf { it.amount }
                val allIncomes = repository.getAllStableIncomes()
                val totalIncome = allIncomes.filter { income ->
                    activeProducts.any { it.id == income.productId }
                }.sumOf { it.amount }

                _uiState.value = StableDetailUiState(
                    account = account,
                    products = activeProducts,
                    totalLocked = totalLocked,
                    totalIncome = totalIncome,
                    expiredProducts = expired,
                    isLoaded = true
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoaded = true)
            }
        }
    }

    fun buyProduct(
        productName: String,
        productType: String,
        amount: Double,
        annualRate: Double,
        purchaseDate: String,
        expireDate: String?,
        onSuccess: () -> Unit
    ) {
        scope.launch {
            try {
                val account = repository.getMainAccountById(accountId) ?: return@launch
                if (amount > account.balance) return@launch
                repository.insertStableProduct(
                    StableProduct(
                        accountId = accountId,
                        productName = productName,
                        productType = productType,
                        amount = amount,
                        annualRate = annualRate,
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

    fun redeemProduct(product: StableProduct, onSuccess: () -> Unit) {
        scope.launch {
            try {
                repository.updateStableProductStatus(product.id, "MATURED")
                loadData()
                onSuccess()
            } catch (_: Exception) {}
        }
    }

    fun recordIncome(productId: Long, amount: Double, date: String, note: String, onSuccess: () -> Unit) {
        scope.launch {
            try {
                repository.insertStableIncome(
                    StableIncome(productId = productId, amount = amount, date = date, note = note)
                )
                loadData()
                onSuccess()
            } catch (_: Exception) {}
        }
    }
}
