package com.anxincaiguan.ui.account

import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountDetailUiState(
    val account: MainAccount? = null,
    val bills: List<Bill> = emptyList(),
    val monthlyExpense: Double = 0.0,
    val isLoading: Boolean = false,
    val showDeleteConfirm: Bill? = null,
    val showBillDialog: Boolean = false
)

class AccountDetailViewModel(
    private val repository: Repository,
    private val accountId: Long
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    fun loadData() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val account = repository.getMainAccountById(accountId)
                val allBills = repository.getBillsByAccount("main", accountId)

                val now = "2026-05"
                val monthlyExpense = allBills
                    .filter { it.type == "EXPENSE" && it.date.startsWith(now) }
                    .sumOf { it.amount }

                _uiState.value = AccountDetailUiState(
                    account = account,
                    bills = allBills,
                    monthlyExpense = monthlyExpense,
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun requestDelete(bill: Bill) {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = bill)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = null)
    }

    fun confirmDelete() {
        val bill = _uiState.value.showDeleteConfirm ?: return
        scope.launch {
            repository.deleteBill(bill.id)
            val delta = if (bill.type == "EXPENSE") bill.amount else -bill.amount
            repository.updateMainAccountBalance(accountId, delta)
            loadData()
        }
        _uiState.value = _uiState.value.copy(showDeleteConfirm = null)
    }

    fun showBillDialog() {
        _uiState.value = _uiState.value.copy(showBillDialog = true)
    }

    fun hideBillDialog() {
        _uiState.value = _uiState.value.copy(showBillDialog = false)
        loadData()
    }

    val accountType: MainAccountType
        get() = MainAccountType.fromName(_uiState.value.account?.name ?: "")
}
