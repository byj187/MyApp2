package com.anxincaiguan.ui.record

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDialog(
    repository: Repository,
    initialType: String = "EXPENSE",
    initialAccountId: Long? = null,
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit = {}
) {
    val viewModel = remember { RecordViewModel(repository) }
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showAccountPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAccounts()
        viewModel.setBillType(initialType)
        viewModel.setDate(getTodayDate())
        if (initialAccountId != null) {
            val account = repository.getMainAccountById(initialAccountId)
            if (account != null) {
                viewModel.selectAccount(AccountOption.Main(account))
            }
        }
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onSaveSuccess()
            onDismiss()
        }
    }

    val expenseCategories = listOf("餐饮", "购物", "交通", "住房", "娱乐", "医疗", "其他")
    val incomeCategories = listOf("工资", "奖金", "投资收益", "其他")
    val categories = if (state.billType == "EXPENSE") expenseCategories else incomeCategories

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = SurfaceLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("记账", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            // ── Type Tabs ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Gray100)
            ) {
                TypeTab(
                    label = "支出",
                    isSelected = state.billType == "EXPENSE",
                    onClick = { viewModel.setBillType("EXPENSE") }
                )
                TypeTab(
                    label = "收入",
                    isSelected = state.billType == "INCOME",
                    onClick = { viewModel.setBillType("INCOME") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Account Selection ──
            Text("选择账户", style = MaterialTheme.typography.labelLarge, color = Gray700)
            Spacer(Modifier.height(8.dp))

            val accounts = if (state.billType == "EXPENSE") viewModel.expenseAccounts()
            else viewModel.incomeAccounts()

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { option ->
                    val isSelected = state.selectedAccount?.let {
                        when {
                            it is AccountOption.Main && option is AccountOption.Main -> it.account.id == option.account.id
                            it is AccountOption.Growth && option is AccountOption.Growth -> it.account.id == option.account.id
                            else -> false
                        }
                    } ?: false

                    val chipColor = when (option) {
                        is AccountOption.Main -> when (com.anxincaiguan.data.model.MainAccountType.fromName(option.account.name)) {
                            com.anxincaiguan.data.model.MainAccountType.DAILY -> DailyColor
                            com.anxincaiguan.data.model.MainAccountType.QUALITY -> QualityColor
                            com.anxincaiguan.data.model.MainAccountType.STABLE -> StableColor
                            com.anxincaiguan.data.model.MainAccountType.GROWTH -> GrowthColor
                        }
                        is AccountOption.Growth -> when (com.anxincaiguan.data.model.GrowthSubAccountType.fromName(option.account.name)) {
                            com.anxincaiguan.data.model.GrowthSubAccountType.INDEX_FUND -> IndexFundColor
                            com.anxincaiguan.data.model.GrowthSubAccountType.ACTIVE_EQUITY -> ActiveEquityColor
                            com.anxincaiguan.data.model.GrowthSubAccountType.BOND_FIXED_INCOME -> BondColor
                            com.anxincaiguan.data.model.GrowthSubAccountType.OTHER_ALTERNATIVE -> AlternativeColor
                        }
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectAccount(option) },
                        label = { Text(
                            if (option is AccountOption.Growth) "📈 ${option.displayName}"
                            else "${option.emoji} ${option.displayName}",
                            style = MaterialTheme.typography.labelSmall
                        ) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.12f),
                            selectedLabelColor = chipColor
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Amount ──
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.setAmount(it) },
                label = { Text("金额") },
                leadingIcon = { Text("¥", style = MaterialTheme.typography.titleLarge) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(16.dp))

            // ── Category ──
            Text("分类", style = MaterialTheme.typography.labelLarge, color = Gray700)
            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { category ->
                    val isSelected = state.category == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(category) },
                        label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue50,
                            selectedLabelColor = Blue600
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Date ──
            OutlinedTextField(
                value = state.date,
                onValueChange = { viewModel.setDate(it) },
                label = { Text("日期") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // ── Note ──
            OutlinedTextField(
                value = state.note,
                onValueChange = { viewModel.setNote(it) },
                label = { Text("备注（选填）") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) }
            )

            Spacer(Modifier.height(24.dp))

            // ── Preview ──
            if (state.selectedAccount != null && state.amount.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Blue50)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = Blue600, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "${if (state.billType == "EXPENSE") "支出" else "收入"} · ${state.selectedAccount!!.emoji} ${state.selectedAccount!!.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray700
                            )
                            if (state.category.isNotEmpty()) {
                                Text(state.category, style = MaterialTheme.typography.labelSmall, color = Gray500)
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${if (state.billType == "EXPENSE") "-" else "+"}¥${state.amount}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (state.billType == "EXPENSE") ErrorColor else SuccessColor
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Buttons ──
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("取消", color = Gray600)
                }
                Button(
                    onClick = { viewModel.save() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                    enabled = state.amount.toDoubleOrNull()?.let { it > 0 } ?: false
                            && state.selectedAccount != null
                            && !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("保存", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    // ── Insufficient Balance Dialog ──
    if (state.showInsufficientDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissInsufficientDialog() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = WarningColor) },
            title = { Text("余额不足", fontWeight = FontWeight.SemiBold) },
            text = { Text("当前账户余额不足以完成本次支出，是否仍要保存？") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmInsufficientSave() },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningColor)
                ) { Text("仍要保存") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissInsufficientDialog() }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun RowScope.TypeTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Blue600 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) Color.White else Gray600
    )
}

private fun getTodayDate(): String {
    return "2026-05-07"
}
