package com.anxincaiguan.ui.report

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import com.anxincaiguan.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    repository: Repository,
    onBack: () -> Unit = {}
) {
    val viewModel = remember { ReportViewModel(repository) }
    val state by viewModel.uiState.collectAsState()
    var showDetailDialog by remember { mutableStateOf<ReportDetailItem?>(null) }

    LaunchedEffect(Unit) { viewModel.loadData() }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("报表", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Time filter chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimeFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = state.timeFilter == filter,
                            onClick = { viewModel.setFilter(filter) },
                            label = { Text(filter.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Blue50,
                                selectedLabelColor = Blue600
                            )
                        )
                    }
                }
            }

            // Summary row
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val totalIncome = state.details.filter { it.amount > 0 }.sumOf { it.amount }
                        val totalExpense = state.details.filter { it.amount < 0 }.sumOf { -it.amount }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("总收入", style = MaterialTheme.typography.labelSmall, color = Gray500)
                            Text("¥${fmt2(totalIncome)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SuccessColor)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("总支出", style = MaterialTheme.typography.labelSmall, color = Gray500)
                            Text("¥${fmt2(totalExpense)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ErrorColor)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("结余", style = MaterialTheme.typography.labelSmall, color = Gray500)
                            Text("¥${fmt2(totalIncome - totalExpense)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Blue600)
                        }
                    }
                }
            }

            // Pie chart
            item {
                ChartCard(title = "四大账户余额分布") {
                    PieChart(
                        data = state.mainAccounts.map { it.balance },
                        labels = state.mainAccounts.map { MainAccountType.fromName(it.name).emoji + " " + it.name },
                        colors = state.mainAccounts.map {
                            when (MainAccountType.fromName(it.name)) {
                                MainAccountType.DAILY -> DailyColor
                                MainAccountType.QUALITY -> QualityColor
                                MainAccountType.STABLE -> StableColor
                                MainAccountType.GROWTH -> GrowthColor
                            }
                        }
                    )
                }
            }

            // Sub-account comparison bar chart
            if (state.growthSubAccounts.isNotEmpty()) {
                item {
                    ChartCard(title = "子账户占比 vs 目标") {
                        SubAccountComparisonChart(
                            subAccounts = state.growthSubAccounts,
                            totalGrowth = state.totalGrowthAmount
                        )
                    }
                }
            }

            // Monthly trend line chart
            if (state.monthlyAggregates.isNotEmpty()) {
                item {
                    ChartCard(title = "月度收支趋势") {
                        MonthlyTrendChart(aggregates = state.monthlyAggregates)
                    }
                }
            }

            // Sub-account income bar chart
            if (state.subAccountIncomes.any { it.second > 0 }) {
                item {
                    ChartCard(title = "各子账户累计收益") {
                        IncomeBarChart(incomes = state.subAccountIncomes)
                    }
                }
            }

            // Detail list
            item {
                Text("明细", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            }

            if (state.details.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = Gray300)
                            Spacer(Modifier.height(8.dp))
                            Text("暂无记录", style = MaterialTheme.typography.bodyMedium, color = Gray400)
                        }
                    }
                }
            } else {
                items(state.details, key = { it.id }) { item ->
                    DetailRow(item = item, onClick = { showDetailDialog = item })
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    showDetailDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDetailDialog = null },
            title = { Text(item.description, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Row { Text("类型：", color = Gray500); Text(item.type) }
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text("金额：", color = Gray500)
                        Text("${if (item.amount >= 0) "+" else ""}¥${fmt2(item.amount)}", fontWeight = FontWeight.Bold,
                            color = if (item.amount > 0) SuccessColor else ErrorColor)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row { Text("日期：", color = Gray500); Text(item.date) }
                    if (item.note.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row { Text("备注：", color = Gray500); Text(item.note) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDetailDialog = null }) { Text("确定") } }
        )
    }
}

// ─── Shared chart card wrapper ─────────────────────────────────────────────
@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
    }
}

// ─── Pie Chart ─────────────────────────────────────────────────────────────
@Composable
private fun PieChart(data: List<Double>, labels: List<String>, colors: List<Color>) {
    val total = data.sum()
    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(160.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sweepAngles = data.map { (it / total * 360).toFloat() }
                var startAngle = -90f
                sweepAngles.forEachIndexed { i, sweep ->
                    drawArc(color = colors[i], startAngle = startAngle, sweepAngle = sweep, useCenter = true, size = Size(size.width, size.height))
                    startAngle += sweep
                }
                drawCircle(color = SurfaceLight, radius = size.width * 0.35f)
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("总资产", style = MaterialTheme.typography.labelSmall, color = Gray500)
                    Text("¥${fmt1(total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        data.forEachIndexed { i, value ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colors[i]))
                Spacer(Modifier.width(8.dp))
                Text(labels[i], modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                Text("${fmt1(value / total * 100)}%", style = MaterialTheme.typography.labelSmall, color = Gray500)
                Spacer(Modifier.width(12.dp))
                Text("¥${fmt0(value)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─── Sub-account comparison bar chart ──────────────────────────────────────
@Composable
private fun SubAccountComparisonChart(subAccounts: List<GrowthSubAccount>, totalGrowth: Double) {
    Column(modifier = Modifier.padding(16.dp)) {
        subAccounts.forEach { sub ->
            val type = GrowthSubAccountType.fromName(sub.name)
            val total = sub.idleAmount + sub.investedAmount
            val actualRatio = if (totalGrowth > 0) total / totalGrowth else 0.0
            val targetRatio = sub.targetRatio
            val color = when (type) {
                GrowthSubAccountType.INDEX_FUND -> IndexFundColor
                GrowthSubAccountType.ACTIVE_EQUITY -> ActiveEquityColor
                GrowthSubAccountType.BOND_FIXED_INCOME -> BondColor
                GrowthSubAccountType.OTHER_ALTERNATIVE -> AlternativeColor
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(type.emoji, modifier = Modifier.width(22.dp))
                Text(sub.name, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(72.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Spacer(Modifier.height(2.dp))
                    // Actual bar
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Gray200)) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(actualRatio.toFloat()).clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.6f)))
                    }
                    Spacer(Modifier.height(2.dp))
                    // Target bar
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Gray200)) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(targetRatio.toFloat()).clip(RoundedCornerShape(4.dp)).background(color))
                    }
                    Spacer(Modifier.height(2.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text("${fmt0(actualRatio * 100)}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, modifier = Modifier.width(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
            Spacer(Modifier.height(6.dp))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(start = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(8.dp, 8.dp).clip(RoundedCornerShape(2.dp)).background(Gray400)); Spacer(Modifier.width(4.dp)); Text("实际", style = MaterialTheme.typography.labelSmall, color = Gray500) }
            Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(8.dp, 8.dp).clip(RoundedCornerShape(2.dp)).background(Gray800)); Spacer(Modifier.width(4.dp)); Text("目标", style = MaterialTheme.typography.labelSmall, color = Gray500) }
        }
    }
}

// ─── Monthly trend line chart ──────────────────────────────────────────────
@Composable
private fun MonthlyTrendChart(aggregates: List<MonthlyAggregate>) {
    val incomeColor = SuccessColor
    val expenseColor = ErrorColor
    Column(modifier = Modifier.padding(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxVal = maxOf(aggregates.maxOfOrNull { it.income } ?: 1.0, aggregates.maxOfOrNull { it.expense } ?: 1.0)
                val stepX = size.width / (aggregates.size * 2)
                val scale = (size.height * 0.8f) / maxVal.toFloat()
                val baseY = size.height * 0.9f

                aggregates.forEachIndexed { i, agg ->
                    val x = i * stepX * 2 + stepX * 0.3f
                    val incomeH = (agg.income * scale).toFloat()
                    val expenseH = (agg.expense * scale).toFloat()
                    drawRect(color = incomeColor, topLeft = Offset(x, baseY - incomeH), size = Size(stepX * 0.6f, incomeH))
                    drawRect(color = expenseColor, topLeft = Offset(x + stepX, baseY - expenseH), size = Size(stepX * 0.6f, expenseH))
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            aggregates.forEach { Text(it.month.takeLast(2) + "月", style = MaterialTheme.typography.labelSmall, color = Gray500) }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(incomeColor)); Spacer(Modifier.width(4.dp)); Text("收入", style = MaterialTheme.typography.labelSmall) }
            Spacer(Modifier.width(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(expenseColor)); Spacer(Modifier.width(4.dp)); Text("支出", style = MaterialTheme.typography.labelSmall) }
        }
    }
}

// ─── Income bar chart ──────────────────────────────────────────────────────
@Composable
private fun IncomeBarChart(incomes: List<Pair<String, Double>>) {
    val maxIncome = incomes.maxOfOrNull { it.second } ?: 1.0
    Column(modifier = Modifier.padding(16.dp)) {
        incomes.forEach { (name, amount) ->
            val type = GrowthSubAccountType.entries.firstOrNull { it.displayName == name } ?: GrowthSubAccountType.INDEX_FUND
            val color = when (type) {
                GrowthSubAccountType.INDEX_FUND -> IndexFundColor
                GrowthSubAccountType.ACTIVE_EQUITY -> ActiveEquityColor
                GrowthSubAccountType.BOND_FIXED_INCOME -> BondColor
                GrowthSubAccountType.OTHER_ALTERNATIVE -> AlternativeColor
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(type.emoji, modifier = Modifier.width(24.dp))
                Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(72.dp))
                Box(modifier = Modifier.weight(1f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(Gray200)) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth((amount / maxIncome).toFloat()).clip(RoundedCornerShape(4.dp)).background(color))
                }
                Spacer(Modifier.width(8.dp))
                Text("¥${fmt0(amount)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, modifier = Modifier.width(56.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
        }
    }
}

// ─── Detail row ────────────────────────────────────────────────────────────
@Composable
private fun DetailRow(item: ReportDetailItem, onClick: () -> Unit) {
    val icon = when (item.type) {
        "支出" -> Icons.Default.ShoppingCart; "收入" -> Icons.Default.AccountBalance
        "调拨" -> Icons.Default.SwapHoriz; "工资" -> Icons.Default.AccountBalanceWallet
        "收益" -> Icons.Default.TrendingUp; else -> Icons.Default.Receipt
    }
    val color = when {
        item.type == "支出" -> ErrorColor; item.amount > 0 -> SuccessColor; else -> Gray500
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
                Text(item.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.date, style = MaterialTheme.typography.labelSmall, color = Gray500)
            }
            Text(
                "${if (item.amount >= 0) "+" else ""}¥${fmt2(item.amount)}",
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = color
            )
        }
    }
}
