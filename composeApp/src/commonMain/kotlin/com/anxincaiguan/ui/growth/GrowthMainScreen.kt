package com.anxincaiguan.ui.growth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.anxincaiguan.data.model.*
import com.anxincaiguan.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthMainScreen(
    repository: Repository,
    onNavigateToSubAccount: (Long) -> Unit = {},
    onNavigateToRebalance: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val viewModel = remember { GrowthMainViewModel(repository) }
    val state by viewModel.uiState.collectAsState()
    var showIncomeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadData() }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("增值投资", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GrowthColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                item { TotalHeader(growthBalance = state.growthAccountBalance, cumulativeIncome = state.cumulativeIncome) }
                item {
                    Text("子账户", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }
                items(state.subAccounts) { sub ->
                    SubAccountCard(sub = sub, onClick = { onNavigateToSubAccount(sub.account.id) })
                }
                item { Spacer(Modifier.height(16.dp)) }
            }

            // Bottom fixed buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = SurfaceLight
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showIncomeDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthColor)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("记录收益", style = MaterialTheme.typography.titleMedium)
                    }
                    OutlinedButton(
                        onClick = onNavigateToRebalance,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("再均衡建议", color = GrowthColor, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    if (showIncomeDialog) {
        IncomeDialog(
            repository = repository,
            subAccounts = state.subAccounts.map { it.account },
            onDismiss = { showIncomeDialog = false },
            onSaveSuccess = { showIncomeDialog = false; viewModel.loadData() }
        )
    }
}

@Composable
private fun TotalHeader(growthBalance: Double, cumulativeIncome: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("增值投资总余额", style = MaterialTheme.typography.labelLarge, color = Gray500)
            Spacer(Modifier.height(4.dp))
            Text("¥${fmt2(growthBalance)}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Gray900)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("累计收益", style = MaterialTheme.typography.labelSmall, color = Gray500)
                    Text("¥${fmt2(cumulativeIncome)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SuccessColor)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("子账户数", style = MaterialTheme.typography.labelSmall, color = Gray500)
                    Text("4", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Gray800)
                }
            }
        }
    }
}

@Composable
private fun SubAccountCard(sub: SubAccountUi, onClick: () -> Unit) {
    val color = when (GrowthSubAccountType.fromName(sub.account.name)) {
        GrowthSubAccountType.INDEX_FUND -> IndexFundColor
        GrowthSubAccountType.ACTIVE_EQUITY -> ActiveEquityColor
        GrowthSubAccountType.BOND_FIXED_INCOME -> BondColor
        GrowthSubAccountType.OTHER_ALTERNATIVE -> AlternativeColor
    }
    val type = GrowthSubAccountType.fromName(sub.account.name)
    val targetBarRatio = sub.targetRatio.toFloat()
    val actualBarRatio = if (sub.total > 0) {
        val totalGrowth = sub.total // simplified; actual total growth is from all subs
        sub.actualRatio.toFloat()
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Text(type.emoji)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(sub.account.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("¥${fmt2(sub.total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    val deviationPct = sub.deviation * 100
                    Text(
                        "${fmt1(sub.actualRatio * 100)}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "目标 ${(sub.targetRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Progress bars: target vs actual
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Gray200)) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(actualBarRatio).clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.7f)))
                }
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Gray200)) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(targetBarRatio).clip(RoundedCornerShape(4.dp)).background(color))
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("当前占比", style = MaterialTheme.typography.labelSmall, color = Gray500)
                val deviationColor = if (sub.isDeviated) ErrorColor else SuccessColor
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("偏离 ", style = MaterialTheme.typography.labelSmall, color = Gray500)
                    Text(
                        "${if (sub.deviation >= 0) "+" else ""}${fmt2(sub.deviation * 100)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = deviationColor
                    )
                }
                Text("目标占比", style = MaterialTheme.typography.labelSmall, color = Gray500)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncomeDialog(
    repository: Repository,
    subAccounts: List<GrowthSubAccount>,
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedSub by remember { mutableStateOf<GrowthSubAccount?>(null) }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-05-07") }
    var note by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = SurfaceLight
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())
        ) {
            Text("记录收益", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            // Sub-account selection
            Text("选择子账户", style = MaterialTheme.typography.labelLarge, color = Gray700)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                subAccounts.forEach { sub ->
                    val type = GrowthSubAccountType.fromName(sub.name)
                    val color = when (type) {
                        GrowthSubAccountType.INDEX_FUND -> IndexFundColor
                        GrowthSubAccountType.ACTIVE_EQUITY -> ActiveEquityColor
                        GrowthSubAccountType.BOND_FIXED_INCOME -> BondColor
                        GrowthSubAccountType.OTHER_ALTERNATIVE -> AlternativeColor
                    }
                    val isSelected = selectedSub?.id == sub.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSub = sub },
                        label = { Text(type.emoji + " " + sub.name, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.12f),
                            selectedLabelColor = color
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^-?\\d*\\.?\\d{0,2}$"))) amount = it },
                label = { Text("收益金额") },
                leadingIcon = { Text("¥", style = MaterialTheme.typography.titleLarge) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(16.dp))

            // Income type
            Text("收益类型", style = MaterialTheme.typography.labelLarge, color = Gray700)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("分红", "利息", "资本利得", "其他").forEach { type ->
                    val isSelected = note == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { note = type },
                        label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GrowthColor.copy(alpha = 0.12f), selectedLabelColor = GrowthColor)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Date
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("日期") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            val amountVal = amount.toDoubleOrNull() ?: 0.0
            val canSave = selectedSub != null && amountVal > 0 && !isSaving

            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        try {
                            val sub = selectedSub!!
                            repository.insertInvestmentIncome(
                                InvestmentIncome(productId = sub.id, amount = amountVal, incomeType = note.ifEmpty { "其他" }, date = date, note = note)
                            )
                            repository.updateGrowthSubAccountIdle(sub.id, amountVal)
                            onSaveSuccess()
                        } catch (_: Exception) {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GrowthColor),
                enabled = canSave
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("保存收益", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
