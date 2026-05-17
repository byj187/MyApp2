package com.anxincaiguan.ui.record

import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecordUiState(
    val billType: String = "EXPENSE",
    val selectedAccount: AccountOption? = null,
    val amount: String = "",
    val category: String = "",
    val date: String = "",
    val note: String = "",
    val mainAccounts: List<MainAccount> = emptyList(),
    val growthSubAccounts: List<GrowthSubAccount> = emptyList(),
    val showInsufficientDialog: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

sealed class AccountOption {
    abstract val displayName: String
    abstract val emoji: String
    data class Main(val account: MainAccount) : AccountOption() {
        override val displayName get() = account.name
        override val emoji get() = MainAccountType.fromName(account.name).emoji
        val balance get() = account.balance
    }
    data class Growth(val account: GrowthSubAccount) : AccountOption() {
        override val displayName get() = account.name
        override val emoji get() = GrowthSubAccountType.fromName(account.name).emoji
        val idleAmount get() = account.idleAmount
    }
}

class RecordViewModel(private val repository: Repository) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    fun loadAccounts() {
        scope.launch {
            val mains = repository.getAllMainAccounts()
            val growthSubs = repository.getAllGrowthSubAccounts()
            _uiState.value = _uiState.value.copy(
                mainAccounts = mains,
                growthSubAccounts = growthSubs
            )
        }
    }

    fun setBillType(type: String) {
        _uiState.value = _uiState.value.copy(
            billType = type,
            selectedAccount = null,
            category = ""
        )
    }

    fun selectAccount(option: AccountOption) {
        _uiState.value = _uiState.value.copy(selectedAccount = option)
    }

    fun setAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.value = _uiState.value.copy(amount = amount)
        }
    }

    fun setCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun setDate(date: String) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun setNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun dismissInsufficientDialog() {
        _uiState.value = _uiState.value.copy(showInsufficientDialog = false)
    }

    fun confirmInsufficientSave() {
        _uiState.value = _uiState.value.copy(showInsufficientDialog = false)
        doSave()
    }

    fun save() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: 0.0

        if (amount <= 0) return
        if (state.selectedAccount == null) return

        if (state.billType == "EXPENSE") {
            val account = state.selectedAccount
            if (account is AccountOption.Main && account.balance < amount) {
                _uiState.value = state.copy(showInsufficientDialog = true)
                return
            }
        }

        doSave()
    }

    private fun doSave() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val state = _uiState.value
                val amount = state.amount.toDoubleOrNull() ?: 0.0
                val account = state.selectedAccount ?: return@launch

                when (account) {
                    is AccountOption.Main -> {
                        val bill = Bill(
                            accountType = "main",
                            accountId = account.account.id,
                            type = state.billType,
                            amount = amount,
                            category = state.category,
                            date = state.date,
                            note = state.note
                        )
                        repository.insertBill(bill)

                        val delta = if (state.billType == "EXPENSE") -amount else amount
                        repository.updateMainAccountBalance(account.account.id, delta)
                    }
                    is AccountOption.Growth -> {
                        val bill = Bill(
                            accountType = "growth",
                            accountId = account.account.id,
                            type = state.billType,
                            amount = amount,
                            category = state.category,
                            date = state.date,
                            note = state.note
                        )
                        repository.insertBill(bill)
                        repository.updateGrowthSubAccountIdle(account.account.id, amount)
                    }
                }

                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun expenseAccounts(): List<AccountOption> {
        return _uiState.value.mainAccounts
            .filter { it.name == MainAccountType.DAILY.displayName || it.name == MainAccountType.QUALITY.displayName }
            .map { AccountOption.Main(it) }
    }

    fun incomeAccounts(): List<AccountOption> {
        val mains = _uiState.value.mainAccounts.map { AccountOption.Main(it) }
        val growths = _uiState.value.growthSubAccounts.map { AccountOption.Growth(it) }
        return mains + growths
    }
}
