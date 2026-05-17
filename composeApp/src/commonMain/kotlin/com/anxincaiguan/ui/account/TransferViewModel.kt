package com.anxincaiguan.ui.account

import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransferUiState(
    val mainAccounts: List<MainAccount> = emptyList(),
    val growthSubAccounts: List<GrowthSubAccount> = emptyList(),
    val fromOption: TransferAccountOption? = null,
    val toOption: TransferAccountOption? = null,
    val amount: String = "",
    val date: String = "2026-05-07",
    val note: String = "",
    val showInsufficientDialog: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

sealed class TransferAccountOption {
    abstract val displayName: String
    abstract val emoji: String
    abstract val availableBalance: Double
    abstract val id: Long
    abstract val type: String
    data class Main(val account: MainAccount) : TransferAccountOption() {
        override val displayName get() = account.name
        override val emoji get() = MainAccountType.fromName(account.name).emoji
        override val availableBalance get() = account.balance
        override val id get() = account.id
        override val type get() = "main"
    }
    data class Growth(val account: GrowthSubAccount) : TransferAccountOption() {
        override val displayName get() = account.name
        override val emoji get() = GrowthSubAccountType.fromName(account.name).emoji
        override val availableBalance get() = account.idleAmount
        override val id get() = account.id
        override val type get() = "growth"
    }
}

class TransferViewModel(private val repository: Repository) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

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

    fun setFrom(option: TransferAccountOption) {
        _uiState.value = _uiState.value.copy(fromOption = option)
    }

    fun setTo(option: TransferAccountOption) {
        _uiState.value = _uiState.value.copy(toOption = option)
    }

    fun setAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.value = _uiState.value.copy(amount = amount)
        }
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

    fun confirmSave() {
        _uiState.value = _uiState.value.copy(showInsufficientDialog = false)
        doSave()
    }

    fun save() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: 0.0
        val from = state.fromOption ?: return
        val to = state.toOption ?: return

        if (amount <= 0) return
        if (from.id == to.id && from.type == to.type) return

        if (from.availableBalance < amount) {
            _uiState.value = state.copy(showInsufficientDialog = true)
            return
        }

        doSave()
    }

    private fun doSave() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val state = _uiState.value
                val amount = state.amount.toDoubleOrNull() ?: 0.0
                val from = state.fromOption ?: return@launch
                val to = state.toOption ?: return@launch

                val transfer = Transfer(
                    fromType = from.type, fromId = from.id,
                    toType = to.type, toId = to.id,
                    amount = amount, date = state.date, note = state.note
                )
                repository.insertTransfer(transfer)

                // From: decrease balance/idle
                when (from) {
                    is TransferAccountOption.Main -> repository.updateMainAccountBalance(from.id, -amount)
                    is TransferAccountOption.Growth -> repository.updateGrowthSubAccountIdle(from.id, -amount)
                }
                // To: increase balance/idle
                when (to) {
                    is TransferAccountOption.Main -> repository.updateMainAccountBalance(to.id, amount)
                    is TransferAccountOption.Growth -> repository.updateGrowthSubAccountIdle(to.id, amount)
                }

                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun allAccounts(): List<TransferAccountOption> {
        val mains = _uiState.value.mainAccounts.map { TransferAccountOption.Main(it) }
        val growths = _uiState.value.growthSubAccounts.map { TransferAccountOption.Growth(it) }
        return mains + growths
    }
}
