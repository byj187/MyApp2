package com.anxincaiguan.ui.profile

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import com.anxincaiguan.ui.theme.*
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(
    repository: Repository,
    onNavigateToOnboarding: () -> Unit = {}
) {
    val viewModel = remember { MineViewModel(repository) }
    val state by viewModel.uiState.collectAsState()
    val subPage by viewModel.subPage.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadData() }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    // Sub-page router
    when (subPage) {
        is MineSubPage.AllocationRules -> AllocationRulesScreen(viewModel, state)
        is MineSubPage.SubAccountRatios -> SubAccountRatiosScreen(viewModel, state)
        is MineSubPage.DeviationThreshold -> DeviationThresholdScreen(viewModel, state)
        is MineSubPage.Budget -> BudgetScreen(viewModel, state)
        is MineSubPage.Security -> SecurityScreen(viewModel)
        is MineSubPage.About -> AboutScreen(viewModel)
        else -> MineMainScreen(viewModel, state, snackbarHostState, onNavigateToOnboarding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MineMainScreen(
    viewModel: MineViewModel,
    state: MineUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateToOnboarding: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(title = { Text("我的", fontWeight = FontWeight.SemiBold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight))
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceLight), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Blue50), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(36.dp), tint = Blue600)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(state.nickname, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("本地账户 · 数据安全", style = MaterialTheme.typography.bodySmall, color = Gray500)
                        }
                    }
                }
            }

            item { SettingsGroup("分配规则") {
                SettingsItem(Icons.Default.PieChart, "存量资产比例", "不影响现有余额") { viewModel.navigateTo(MineSubPage.AllocationRules) }
                SettingsItem(Icons.Default.AccountBalanceWallet, "工资分配规则", "固定+剩余比例") { viewModel.navigateTo(MineSubPage.AllocationRules) }
            }}
            item { SettingsGroup("投资设置") {
                SettingsItem(Icons.Default.AccountTree, "子账户比例", "修改后建议再均衡") { viewModel.navigateTo(MineSubPage.SubAccountRatios) }
                SettingsItem(Icons.Default.Tune, "偏离阈值", "${fmt0(state.deviationThreshold.toDouble())}%") { viewModel.navigateTo(MineSubPage.DeviationThreshold) }
                SettingsItem(Icons.Default.Savings, "预算管理", "设置月度预算") { viewModel.navigateTo(MineSubPage.Budget) }
            }}
            item { SettingsGroup("数据管理") {
                SettingsItem(Icons.Default.Backup, "备份数据", "导出 JSON") { viewModel.navigateTo(MineSubPage.Backup) }
                SettingsItem(Icons.Default.Restore, "恢复数据", "导入 JSON") { viewModel.navigateTo(MineSubPage.Restore) }
            }}
            item { SettingsGroup("安全") {
                SettingsItem(Icons.Default.Lock, "安全设置", "应用锁/理财密码") { viewModel.navigateTo(MineSubPage.Security) }
            }}
            item { SettingsGroup("关于") {
                SettingsItem(Icons.Default.Info, "关于我们", "v1.0.0") { viewModel.navigateTo(MineSubPage.About) }
            }}

            item {
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceLight)) {
                    ListItem(
                        headlineContent = { Text("清空所有数据", color = ErrorColor, fontWeight = FontWeight.Medium) },
                        leadingContent = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ErrorColor) },
                        modifier = Modifier.clickable { viewModel.navigateTo(MineSubPage.AllocationRules) } // placeholder
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─── Reusable Components ───────────────────────────────────────────────────
@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Gray600, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceLight), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(4.dp)) { content() }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(subtitle, color = Gray500, style = MaterialTheme.typography.bodySmall) },
        leadingContent = { Icon(icon, contentDescription = null, tint = Blue600) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Gray400) },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

// ─── Sub-Pages ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllocationRulesScreen(viewModel: MineViewModel, state: MineUiState) {
    var dailyFixed by remember { mutableStateOf(state.dailyFixedAmount) }
    var qualityR by remember { mutableFloatStateOf((state.salaryRatios["QUALITY"]?.let { (it * 100).toFloat() } ?: 30f)) }
    var stableR by remember { mutableFloatStateOf((state.salaryRatios["STABLE"]?.let { (it * 100).toFloat() } ?: 40f)) }
    var growthR by remember { mutableFloatStateOf((state.salaryRatios["GROWTH"]?.let { (it * 100).toFloat() } ?: 30f)) }
    val total = qualityR + stableR + growthR

    SubPageScaffold(title = "分配规则", onBack = { viewModel.navigateBack() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text("存量资产比例", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("修改比例不影响现有余额，仅影响后续资产录入", style = MaterialTheme.typography.bodySmall, color = Gray500)
            Spacer(Modifier.height(16.dp))
            RatioRow("💳 日常消费", state.stockRatios["DAILY"]?.let { "${(it * 100).toInt()}%" } ?: "20%", DailyColor)
            RatioRow("🎭 生活品质", state.stockRatios["QUALITY"]?.let { "${(it * 100).toInt()}%" } ?: "20%", QualityColor)
            RatioRow("🛡稳健保底", state.stockRatios["STABLE"]?.let { "${(it * 100).toInt()}%" } ?: "30%", StableColor)
            RatioRow("📈 增值投资", state.stockRatios["GROWTH"]?.let { "${(it * 100).toInt()}%" } ?: "30%", GrowthColor)

            Spacer(Modifier.height(24.dp))
            Text("工资分配规则", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = dailyFixed, onValueChange = { dailyFixed = it }, label = { Text("日常固定金额") }, leadingIcon = { Text("¥") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(Modifier.height(12.dp))
            Text("剩余部分：${total.roundToLong()}%", style = MaterialTheme.typography.labelMedium, color = if (total == 100f) SuccessColor else ErrorColor)
            Spacer(Modifier.height(8.dp))
            RatioSlider("🎭 生活品质", qualityR, QualityColor) { qualityR = it; val r = 100f - stableR - growthR; qualityR = it.coerceIn(0f, r) }
            RatioSlider("🛡稳健保底", stableR, StableColor) { stableR = it; val r = 100f - qualityR - growthR; stableR = it.coerceIn(0f, r) }
            RatioSlider("📈 增值投资", growthR, GrowthColor) { growthR = it; val r = 100f - qualityR - stableR; growthR = it.coerceIn(0f, r) }

            Spacer(Modifier.height(24.dp))
            Button(onClick = { viewModel.saveAllocationRules(dailyFixed, qualityR, stableR, growthR) }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600), enabled = total == 100f) {
                Text("保存", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubAccountRatiosScreen(viewModel: MineViewModel, state: MineUiState) {
    var indexR by remember { mutableFloatStateOf((state.growthRatios["INDEX_FUND"]?.let { (it * 100).toFloat() } ?: 50f)) }
    var activeR by remember { mutableFloatStateOf((state.growthRatios["ACTIVE_EQUITY"]?.let { (it * 100).toFloat() } ?: 30f)) }
    var bondR by remember { mutableFloatStateOf((state.growthRatios["BOND_FIXED_INCOME"]?.let { (it * 100).toFloat() } ?: 15f)) }
    var altR by remember { mutableFloatStateOf((state.growthRatios["OTHER_ALTERNATIVE"]?.let { (it * 100).toFloat() } ?: 5f)) }
    val total = indexR + activeR + bondR + altR

    SubPageScaffold(title = "子账户比例", onBack = { viewModel.navigateBack() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text("修改四个子账户的目标比例", style = MaterialTheme.typography.bodyMedium, color = Gray500)
            Text("总和：${total.roundToLong()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (total == 100f) SuccessColor else ErrorColor)
            Spacer(Modifier.height(12.dp))
            RatioSlider("📊 指数基金", indexR, IndexFundColor) { indexR = it; val r = 100f - activeR - bondR - altR; indexR = it.coerceIn(0f, r) }
            RatioSlider("🎯 主动权益基金", activeR, ActiveEquityColor) { activeR = it; val r = 100f - indexR - bondR - altR; activeR = it.coerceIn(0f, r) }
            RatioSlider("🔒 债券/固收", bondR, BondColor) { bondR = it; val r = 100f - indexR - activeR - altR; bondR = it.coerceIn(0f, r) }
            RatioSlider("💎 其他另类", altR, AlternativeColor) { altR = it; val r = 100f - indexR - activeR - bondR; altR = it.coerceIn(0f, r) }

            Spacer(Modifier.height(24.dp))
            Button(onClick = { viewModel.saveSubAccountRatios(indexR, activeR, bondR, altR) }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600), enabled = total == 100f) {
                Text("保存并建议再均衡", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviationThresholdScreen(viewModel: MineViewModel, state: MineUiState) {
    var threshold by remember { mutableFloatStateOf(state.deviationThreshold) }

    SubPageScaffold(title = "偏离阈值", onBack = { viewModel.navigateBack() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("当子账户实际占比与目标占比的偏离超过阈值时，首页将显示再均衡提示", style = MaterialTheme.typography.bodyMedium, color = Gray600)
            Spacer(Modifier.height(32.dp))
            Text("当前阈值：${fmt0(threshold.toDouble())}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Blue600)
            Spacer(Modifier.height(16.dp))
            Slider(value = threshold, onValueChange = { threshold = it }, valueRange = 0f..20f, steps = 39, colors = SliderDefaults.colors(thumbColor = Blue600, activeTrackColor = Blue600))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("0%", style = MaterialTheme.typography.labelSmall, color = Gray500)
                Text("20%", style = MaterialTheme.typography.labelSmall, color = Gray500)
            }
            Spacer(Modifier.height(32.dp))
            Button(onClick = { viewModel.saveDeviationThreshold(threshold) }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600)) {
                Text("保存", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetScreen(viewModel: MineViewModel, state: MineUiState) {
    var budget by remember { mutableStateOf(state.monthlyBudget) }

    SubPageScaffold(title = "预算管理", onBack = { viewModel.navigateBack() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text("设置月度预算，超出时首页将显示超支提示", style = MaterialTheme.typography.bodyMedium, color = Gray600)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("月度预算") }, leadingIcon = { Text("¥") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(Modifier.height(24.dp))
            Button(onClick = { viewModel.navigateBack() }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600)) {
                Text("保存", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecurityScreen(viewModel: MineViewModel) {
    var appLock by remember { mutableStateOf(false) }
    var passwordEnabled by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    SubPageScaffold(title = "安全设置", onBack = { viewModel.navigateBack() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceLight)) {
                Column(modifier = Modifier.padding(4.dp)) {
                    ListItem(
                        headlineContent = { Text("启用应用锁") },
                        supportingContent = { Text("打开应用时需要验证", color = Gray500) },
                        leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = Blue600) },
                        trailingContent = { Switch(checked = appLock, onCheckedChange = { appLock = it }) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("理财密码") },
                        supportingContent = { Text(if (passwordEnabled) "已设置" else "未设置", color = Gray500) },
                        leadingContent = { Icon(Icons.Default.Password, contentDescription = null, tint = Blue600) },
                        trailingContent = { Switch(checked = passwordEnabled, onCheckedChange = { passwordEnabled = it }) }
                    )
                }
            }
            if (passwordEnabled) {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("设置理财密码") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, visualTransformation = PasswordVisualTransformation())
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.navigateBack() }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600)) {
                    Text("保存", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Blue50)) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Blue600, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("所有数据仅存储在本地设备，不会上传到任何服务器。", style = MaterialTheme.typography.bodySmall, color = Gray700)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutScreen(viewModel: MineViewModel) {
    SubPageScaffold(title = "关于我们", onBack = { viewModel.navigateBack() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(32.dp))
            Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Blue50), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(44.dp), tint = Blue600)
            }
            Spacer(Modifier.height(16.dp))
            Text("安心财管", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("v1.0.0", style = MaterialTheme.typography.bodyMedium, color = Gray500)
            Spacer(Modifier.height(32.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceLight)) {
                Column(modifier = Modifier.padding(4.dp)) {
                    ListItem(headlineContent = { Text("版本号") }, supportingContent = { Text("1.0.0", color = Gray500) })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(headlineContent = { Text("隐私政策") }, supportingContent = { Text("纯本地应用，无需网络", color = Gray500) }, trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Gray400) })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(headlineContent = { Text("技术支持") }, supportingContent = { Text("anzincaiguan@support.com", color = Gray500) })
                }
            }
        }
    }
}

// ─── Shared helpers ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubPageScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            content()
        }
    }
}

@Composable
private fun RatioRow(emojiLabel: String, ratio: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(emojiLabel, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(ratio, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun RatioSlider(label: String, value: Float, color: Color, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${value.roundToLong()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..100f, steps = 99, colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color))
    }
}
