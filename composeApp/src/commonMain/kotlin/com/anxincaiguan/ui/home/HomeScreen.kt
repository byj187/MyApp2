package com.anxincaiguan.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.MainAccount
import com.anxincaiguan.data.model.MainAccountType
import com.anxincaiguan.ui.profile.MineScreen
import com.anxincaiguan.ui.record.RecordDialog
import com.anxincaiguan.ui.record.SalaryDialog
import com.anxincaiguan.ui.report.ReportScreen
import com.anxincaiguan.ui.theme.*

enum class BottomTab(val label: String, val icon: ImageVector) {
    HOME("首页", Icons.Default.Home),
    REPORT("报表", Icons.Default.BarChart),
    PROFILE("我的", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: Repository,
    onNavigateToAccount: (Long) -> Unit = {},
    onNavigateToGrowth: () -> Unit = {},
    onNavigateToRebalance: () -> Unit = {},
    onNavigateToTransfer: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {}
) {
    val viewModel = remember { HomeViewModel(repository) }
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showSalaryDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf<DynamicItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceLight,
                tonalElevation = 2.dp
            ) {
                BottomTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Blue600,
                            selectedTextColor = Blue600,
                            indicatorColor = Blue50
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                BottomTab.HOME -> HomeTab(
                    state = state,
                    onExpenseClick = { showExpenseDialog = true },
                    onIncomeClick = { showIncomeDialog = true },
                    onTransferClick = onNavigateToTransfer,
                    onSalaryClick = { showSalaryDialog = true },
                    onAccountClick = { account ->
                        if (account.name == MainAccountType.GROWTH.displayName) onNavigateToGrowth()
                        else onNavigateToAccount(account.id)
                    },
                    onRebalanceClick = onNavigateToRebalance,
                    onDynamicItemClick = { showDetailDialog = it }
                )
                BottomTab.REPORT -> ReportScreen(
                    repository = repository,
                    onBack = {}
                )
                BottomTab.PROFILE -> MineScreen(
                    repository = repository,
                    onNavigateToOnboarding = {}
                )
            }
        }
    }

    if (showExpenseDialog) RecordDialog(
        repository = repository,
        initialType = "EXPENSE",
        onDismiss = { showExpenseDialog = false; viewModel.loadData() },
        onSaveSuccess = { viewModel.loadData() }
    )
    if (showIncomeDialog) RecordDialog(
        repository = repository,
        initialType = "INCOME",
        onDismiss = { showIncomeDialog = false; viewModel.loadData() },
        onSaveSuccess = { viewModel.loadData() }
    )
    if (showSalaryDialog) SalaryDialog(
        repository = repository,
        onDismiss = { showSalaryDialog = false; viewModel.loadData() },
        onSaveSuccess = { viewModel.loadData() }
    )
    showDetailDialog?.let { item ->
        DetailDialog(item = item, onDismiss = { showDetailDialog = null })
    }
}

@Composable
private fun HomeTab(
    state: HomeUiState,
    onExpenseClick: () -> Unit,
    onIncomeClick: () -> Unit,
    onTransferClick: () -> Unit,
    onSalaryClick: () -> Unit,
    onAccountClick: (MainAccount) -> Unit,
    onRebalanceClick: () -> Unit,
    onDynamicItemClick: (DynamicItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundLight)
    ) {
        item { TotalAssetsCard(totalAssets = state.totalAssets) }
        item { QuickActions(onExpenseClick, onIncomeClick, onTransferClick, onSalaryClick) }
        if (state.isRebalanceNeeded) {
            item { RebalanceBanner(onClick = onRebalanceClick) }
        }
        item {
            SectionTitle("我的账户")
            Spacer(Modifier.height(8.dp))
        }
        item { AccountGrid(accounts = state.mainAccounts, onAccountClick = onAccountClick) }
        item { Spacer(Modifier.height(8.dp)) }
        item { SectionTitle("最近动态") }
        items(state.dynamicItems) { item ->
            DynamicFlowItem(item = item, onClick = { onDynamicItemClick(item) })
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun TotalAssetsCard(totalAssets: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blue600)
            .padding(top = 16.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
    ) {
        Column {
            Text("总资产", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(4.dp))
            Text(
                "¥" + fmt2(totalAssets),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun QuickActions(
    onExpense: () -> Unit,
    onIncome: () -> Unit,
    onTransfer: () -> Unit,
    onSalary: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-18).dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            item { QuickActionBtn(icon = Icons.Default.ShoppingCart, label = "记支出", color = ErrorColor, onClick = onExpense) }
            item { QuickActionBtn(icon = Icons.Default.AddCircle, label = "记收入", color = SuccessColor, onClick = onIncome) }
            item { QuickActionBtn(icon = Icons.Default.SwapHoriz, label = "资金调拨", color = Orange500, onClick = onTransfer) }
            item { QuickActionBtn(icon = Icons.Default.AccountBalanceWallet, label = "工资分配", color = Blue600, onClick = onSalary) }
        }
    }
}

@Composable
private fun QuickActionBtn(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Gray600, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RebalanceBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarningColor.copy(alpha = 0.08f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = WarningColor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("再均衡提醒", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Gray800)
                Text("部分子账户偏离目标比例，建议调整", style = MaterialTheme.typography.bodySmall, color = Gray600)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Gray400)
        }
    }
}

@Composable
private fun AccountGrid(accounts: List<MainAccount>, onAccountClick: (MainAccount) -> Unit) {
    if (accounts.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight)
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("暂无账户数据，请先完成引导设置", style = MaterialTheme.typography.bodyMedium, color = Gray400)
            }
        }
        return
    }
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        accounts.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { account ->
                    AccountCard(account = account, onClick = { onAccountClick(account) }, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AccountCard(account: MainAccount, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = when (MainAccountType.fromName(account.name)) {
        MainAccountType.DAILY -> DailyColor
        MainAccountType.QUALITY -> QualityColor
        MainAccountType.STABLE -> StableColor
        MainAccountType.GROWTH -> GrowthColor
    }
    Card(
        modifier = modifier.height(110.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Text(
                        when (MainAccountType.fromName(account.name)) {
                            MainAccountType.DAILY -> "💳"
                            MainAccountType.QUALITY -> "🎭"
                            MainAccountType.STABLE -> "🛡️"
                            MainAccountType.GROWTH -> "📈"
                        },
                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(account.name, style = MaterialTheme.typography.labelMedium, color = Gray600)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "¥" + fmt2(account.balance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DynamicFlowItem(item: DynamicItem, onClick: () -> Unit) {
    val icon = when (item.type) {
        "支出" -> Icons.Default.ShoppingCart
        "收入" -> Icons.Default.AccountBalance
        "调拨" -> Icons.Default.SwapHoriz
        "工资" -> Icons.Default.AccountBalanceWallet
        "收益" -> Icons.Default.TrendingUp
        else -> Icons.Default.Receipt
    }
    val color = when {
        item.type == "支出" -> ErrorColor
        item.amount > 0 -> SuccessColor
        else -> Gray500
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.note.isNotEmpty()) {
                    Text(item.note, style = MaterialTheme.typography.labelSmall, color = Gray500, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                    val sign = if (item.amount >= 0) "+" else ""
                    val amtText = sign + "¥" + fmt2(item.amount)
                    Text(
                        text = amtText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
                Text(item.date.takeLast(5), style = MaterialTheme.typography.labelSmall, color = Gray400)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    )
}

@Composable
private fun DetailDialog(item: DynamicItem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.title, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Row { Text("类型：", color = Gray500); Text(item.type) }
                Spacer(Modifier.height(4.dp))
                Row { Text("金额：", color = Gray500); val sign = if (item.amount >= 0) "+" else ""; Text(text = sign + "¥" + fmt2(item.amount), fontWeight = FontWeight.Bold, color = if (item.amount > 0) SuccessColor else ErrorColor) }
                Spacer(Modifier.height(4.dp))
                Row { Text("日期：", color = Gray500); Text(item.date) }
                if (item.note.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row { Text("备注：", color = Gray500); Text(item.note) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("确定") } }
    )
}


