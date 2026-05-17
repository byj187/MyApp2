package com.anxincaiguan.ui.account

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.Bill
import com.anxincaiguan.data.model.MainAccountType
import com.anxincaiguan.ui.record.RecordDialog
import com.anxincaiguan.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: Long,
    repository: Repository,
    onBack: () -> Unit = {},
    onNavigateToStableDetail: () -> Unit = {}
) {
    val viewModel = remember { AccountDetailViewModel(repository, accountId) }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadData() }

    val accountType = MainAccountType.fromName(state.account?.name ?: "")
    val color = when (accountType) {
        MainAccountType.DAILY -> DailyColor
        MainAccountType.QUALITY -> QualityColor
        MainAccountType.STABLE -> StableColor
        MainAccountType.GROWTH -> GrowthColor
    }

    if (state.showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = WarningColor) },
            title = { Text(text = "确认删除", fontWeight = FontWeight.SemiBold) },
            text = { Text(text = "删除后账户余额将自动回滚。确定要删除此记录吗？") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) { Text(text = "删除") }
            },
            dismissButton = { TextButton(onClick = { viewModel.cancelDelete() }) { Text(text = "取消") } }
        )
    }

    if (state.showBillDialog) {
        RecordDialog(
            repository = repository,
            initialType = "EXPENSE",
            initialAccountId = accountId,
            onDismiss = { viewModel.hideBillDialog() },
            onSaveSuccess = { viewModel.hideBillDialog() }
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text(text = accountType.emoji + " " + accountType.displayName, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") }
                },
                actions = {
                    IconButton(onClick = { viewModel.showBillDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "记一笔", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = color,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "账户余额", style = MaterialTheme.typography.labelLarge, color = Gray500)
                        Spacer(Modifier.height(8.dp))
                        val balanceText: String = "¥" + fmt2(state.account?.balance ?: 0.0)
                        Text(
                            text = balanceText,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val totalIncome = state.bills.filter { it.type == "INCOME" }.sumOf { it.amount }
                            val totalExpense = state.bills.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                            val incomeText: String = "¥" + fmt2(totalIncome)
                            val expenseText: String = "¥" + fmt2(totalExpense)
                            val monthlyText: String = "¥" + fmt2(state.monthlyExpense)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "总收入", style = MaterialTheme.typography.labelSmall, color = Gray500)
                                Text(
                                    text = incomeText,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessColor
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "总支出", style = MaterialTheme.typography.labelSmall, color = Gray500)
                                Text(
                                    text = expenseText,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorColor
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "本月支出", style = MaterialTheme.typography.labelSmall, color = Gray500)
                                Text(
                                    text = monthlyText,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorColor
                                )
                            }
                        }
                    }
                }
            }

            item {
                if (accountType == MainAccountType.STABLE) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable(onClick = onNavigateToStableDetail),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StableColor.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = StableColor, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(text = "管理稳健产品", style = MaterialTheme.typography.labelLarge, color = StableColor, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { viewModel.showBillDialog() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(text = "记一笔", style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "资金流水",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            if (state.bills.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = Gray300)
                            Spacer(Modifier.height(8.dp))
                            Text(text = "暂无流水", style = MaterialTheme.typography.bodyMedium, color = Gray400)
                        }
                    }
                }
            } else {
                items(state.bills, key = { it.id }) { bill ->
                    BillItem(
                        bill = bill,
                        onClick = { viewModel.requestDelete(bill) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun BillItem(bill: Bill, onClick: () -> Unit = {}) {
    val color = if (bill.type == "EXPENSE") ErrorColor else SuccessColor
    val icon = if (bill.type == "EXPENSE") Icons.Default.ShoppingCart else Icons.Default.AccountBalance

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row {
                    Text(text = bill.date, style = MaterialTheme.typography.labelSmall, color = Gray500)
                    if (bill.note.isNotEmpty()) {
                        Text(text = " \u00B7 " + bill.note, style = MaterialTheme.typography.labelSmall, color = Gray500, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val sign = if (bill.type == "INCOME") "+" else "-"
                val amountText: String = sign + "¥" + fmt2(bill.amount)
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}
