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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import com.anxincaiguan.ui.theme.*
import kotlinx.coroutines.launch

private enum class ActionType { BUY, REDEEM, TRANSFER, INCOME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthSubDetailScreen(
    subAccountId: Long,
    repository: Repository,
    onBack: () -> Unit = {}
) {
    val viewModel = remember { GrowthSubDetailViewModel(repository, subAccountId) }
    val state by viewModel.uiState.collectAsState()
    var activeAction by remember { mutableStateOf<ActionType?>(null) }

    LaunchedEffect(Unit) { viewModel.loadData() }

    val sub = state.subAccount
    val type = if (sub != null) GrowthSubAccountType.fromName(sub.name) else GrowthSubAccountType.INDEX_FUND
    val color = when (type) {
        GrowthSubAccountType.INDEX_FUND -> IndexFundColor
        GrowthSubAccountType.ACTIVE_EQUITY -> ActiveEquityColor
        GrowthSubAccountType.BOND_FIXED_INCOME -> BondColor
        GrowthSubAccountType.OTHER_ALTERNATIVE -> AlternativeColor
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("${type.emoji} ${type.displayName}", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = color, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Header card
                item { HeaderCard(sub = sub, total = state.total, color = color) }

                // Action buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ActionButton(label = "记录投资", icon = Icons.Default.AddCircle, color = color) { activeAction = ActionType.BUY }
                        ActionButton(label = "调拨资金", icon = Icons.Default.SwapHoriz, color = color) { activeAction = ActionType.TRANSFER }
                        ActionButton(label = "记录收益", icon = Icons.Default.TrendingUp, color = color) { activeAction = ActionType.INCOME }
                        ActionButton(label = "赎回", icon = Icons.Default.RemoveCircle, color = color) { activeAction = ActionType.REDEEM }
                    }
                }

                // Products section
                item {
                    Text("持有产品 (${state.products.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }

                if (state.products.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(48.dp), tint = Gray300)
                                Spacer(Modifier.height(8.dp))
                                Text("暂无产品", style = MaterialTheme.typography.bodyMedium, color = Gray400)
                            }
                        }
                    }
                } else {
                    items(state.products, key = { it.id }) { prod ->
                        ProductCard(product = prod, color = color)
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    // Dialogs
    when (activeAction) {
        ActionType.BUY -> BuyDialog(repository = repository, subAccountId = subAccountId, idleAmount = sub?.idleAmount ?: 0.0, color = color,
            onDismiss = { activeAction = null }, onSave = { activeAction = null; viewModel.loadData() })
        ActionType.REDEEM -> RedeemDialog(repository = repository, subAccountId = subAccountId, products = state.products, color = color,
            onDismiss = { activeAction = null }, onSave = { activeAction = null; viewModel.loadData() })
        ActionType.TRANSFER -> TransferDialog(repository = repository, subAccountId = subAccountId, idleAmount = sub?.idleAmount ?: 0.0, color = color,
            onDismiss = { activeAction = null }, onSave = { activeAction = null; viewModel.loadData() })
        ActionType.INCOME -> IncomeSubDialog(repository = repository, subAccountId = subAccountId, products = state.products, color = color,
            onDismiss = { activeAction = null }, onSave = { activeAction = null; viewModel.loadData() })
        null -> {}
    }
}

@Composable
private fun HeaderCard(sub: GrowthSubAccount?, total: Double, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("账户总额", style = MaterialTheme.typography.labelLarge, color = Gray500)
            Spacer(Modifier.height(4.dp))
            Text("¥${fmt2(total)}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("待投资", style = MaterialTheme.typography.labelSmall, color = Gray500)
                    Text("¥${fmt2(sub?.idleAmount ?: 0.0)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Gray800)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("已投资", style = MaterialTheme.typography.labelSmall, color = Gray500)
                    Text("¥${fmt2(sub?.investedAmount ?: 0.0)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("目标占比", style = MaterialTheme.typography.labelSmall, color = Gray500)
                    Text("${((sub?.targetRatio ?: 0.0) * 100).toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
            if (total > 0) {
                Spacer(Modifier.height(12.dp))
                val investedPct = ((sub?.investedAmount ?: 0.0) / total).toFloat()
                LinearProgressIndicator(progress = { investedPct }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = color, trackColor = Gray200)
                Text("已投占比 ${fmt1(investedPct.toDouble() * 100)}%", style = MaterialTheme.typography.labelSmall, color = Gray400,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
        }
    }
}

@Composable
private fun RowScope.ActionButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.08f)).clickable(onClick = onClick).padding(vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ProductCard(product: InvestmentProduct, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Text(product.productName.take(1), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("购买日 " + product.purchaseDate + (if (product.rate != 0.0) " · " + fmt1(product.rate * 100) + "%" else ""),
                    style = MaterialTheme.typography.labelSmall, color = Gray500)
            }
            Text("¥${fmt2(product.amount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Buy Dialog ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuyDialog(repository: Repository, subAccountId: Long, idleAmount: Double, color: Color, onDismiss: () -> Unit, onSave: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-05-07") }
    var expire by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), containerColor = SurfaceLight) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text("记录投资", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("可投资金额：¥${fmt2(idleAmount)}", style = MaterialTheme.typography.bodySmall, color = Gray500)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("产品名称") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = amount, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it }, label = { Text("投资金额") },
                leadingIcon = { Text("¥") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = rate, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) rate = it }, label = { Text("收益率(%)") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("购买日期") }, leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = expire, onValueChange = { expire = it }, label = { Text("到期日（选填）") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)

            Spacer(Modifier.height(24.dp))
            val amt = amount.toDoubleOrNull() ?: 0.0
            Button(onClick = {
                scope.launch {
                    val sub = repository.getGrowthSubAccountById(subAccountId) ?: return@launch
                    if (amt <= sub.idleAmount) {
                        repository.updateGrowthSubAccountIdle(subAccountId, -amt)
                        repository.updateGrowthSubAccountInvested(subAccountId, amt)
                        repository.insertInvestmentProduct(InvestmentProduct(subAccountId = subAccountId, productName = name, amount = amt, rate = rate.toDoubleOrNull() ?: 0.0, purchaseDate = date, expireDate = expire.ifEmpty { null }, status = "ACTIVE"))
                        onSave()
                    }
                }
            }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = color), enabled = name.isNotEmpty() && amt > 0 && amt <= idleAmount) {
                Text("确认投资", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// ─── Redeem Dialog ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RedeemDialog(repository: Repository, subAccountId: Long, products: List<InvestmentProduct>, color: Color, onDismiss: () -> Unit, onSave: () -> Unit) {
    val scope = rememberCoroutineScope()
    var selectedProduct by remember { mutableStateOf<InvestmentProduct?>(null) }
    var redeemAmount by remember { mutableStateOf("") }
    var capitalGain by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), containerColor = SurfaceLight) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text("赎回/卖出", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            Text("选择产品", style = MaterialTheme.typography.labelLarge, color = Gray700)
            Spacer(Modifier.height(8.dp))
            products.forEach { prod ->
                val isSel = selectedProduct?.id == prod.id
                FilterChip(selected = isSel, onClick = { selectedProduct = prod }, label = { Text("${prod.productName} (¥${fmt0(prod.amount)})", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.padding(vertical = 2.dp), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color.copy(alpha = 0.12f), selectedLabelColor = color))
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = redeemAmount, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) redeemAmount = it },
                label = { Text("赎回金额") }, leadingIcon = { Text("¥") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = capitalGain, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) capitalGain = it },
                label = { Text("资本利得（选填）") }, leadingIcon = { Text("¥") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

            Spacer(Modifier.height(24.dp))
            val rAmt = redeemAmount.toDoubleOrNull() ?: 0.0
            val cGain = capitalGain.toDoubleOrNull() ?: 0.0
            val maxRedeem = selectedProduct?.amount ?: 0.0
            Button(onClick = {
                scope.launch {
                    val prod = selectedProduct ?: return@launch
                    if (rAmt > prod.amount) return@launch
                    val newAmt = prod.amount - rAmt
                    if (newAmt <= 0.01) { repository.updateProductStatus(prod.id, "REDEEMED"); repository.updateProductAmount(prod.id, 0.0) }
                    else { repository.updateProductAmount(prod.id, newAmt) }
                    repository.updateGrowthSubAccountInvested(subAccountId, -rAmt)
                    repository.updateGrowthSubAccountIdle(subAccountId, rAmt)
                    if (cGain > 0) {
                        repository.insertInvestmentIncome(InvestmentIncome(productId = prod.id, amount = cGain, incomeType = "资本利得", date = "2026-05-07", note = "赎回-${prod.productName}"))
                        repository.updateGrowthSubAccountIdle(subAccountId, cGain)
                    }
                    onSave()
                }
            }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = color), enabled = selectedProduct != null && rAmt > 0 && rAmt <= maxRedeem) {
                Text("确认赎回", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// ─── Transfer Dialog ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransferDialog(repository: Repository, subAccountId: Long, idleAmount: Double, color: Color, onDismiss: () -> Unit, onSave: () -> Unit) {
    val scope = rememberCoroutineScope()
    var allSubs by remember { mutableStateOf<List<GrowthSubAccount>>(emptyList()) }
    var targetSub by remember { mutableStateOf<GrowthSubAccount?>(null) }
    var amount by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { allSubs = repository.getAllGrowthSubAccounts().filter { it.id != subAccountId } }

    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), containerColor = SurfaceLight) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text("调拨资金", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("可调拨：¥${fmt2(idleAmount)}", style = MaterialTheme.typography.bodySmall, color = Gray500)
            Spacer(Modifier.height(16.dp))

            Text("目标子账户", style = MaterialTheme.typography.labelLarge, color = Gray700)
            Spacer(Modifier.height(8.dp))
            allSubs.forEach { sub ->
                val t = GrowthSubAccountType.fromName(sub.name)
                val isSel = targetSub?.id == sub.id
                FilterChip(selected = isSel, onClick = { targetSub = sub }, label = { Text("${t.emoji} ${sub.name}", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color.copy(alpha = 0.12f), selectedLabelColor = color))
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = amount, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                label = { Text("调拨金额") }, leadingIcon = { Text("¥") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

            Spacer(Modifier.height(24.dp))
            val amt = amount.toDoubleOrNull() ?: 0.0
            Button(onClick = {
                scope.launch {
                    val target = targetSub ?: return@launch
                    if (amt > idleAmount) return@launch
                    repository.updateGrowthSubAccountIdle(subAccountId, -amt)
                    repository.updateGrowthSubAccountIdle(target.id, amt)
                    repository.insertTransfer(Transfer(fromType = "growth", fromId = subAccountId, toType = "growth", toId = target.id, amount = amt, date = "2026-05-07", note = "子账户调拨"))
                    onSave()
                }
            }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = color), enabled = targetSub != null && amt > 0 && amt <= idleAmount) {
                Text("确认调拨", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// ─── Income Dialog ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncomeSubDialog(repository: Repository, subAccountId: Long, products: List<InvestmentProduct>, color: Color, onDismiss: () -> Unit, onSave: () -> Unit) {
    val scope = rememberCoroutineScope()
    var selectedProductId by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf("") }
    var incomeType by remember { mutableStateOf("分红") }
    var date by remember { mutableStateOf("2026-05-07") }

    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), containerColor = SurfaceLight) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text("记录收益", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            Text("关联产品（选填）", style = MaterialTheme.typography.labelLarge, color = Gray700)
            Spacer(Modifier.height(8.dp))
            FilterChip(selected = selectedProductId == null, onClick = { selectedProductId = null }, label = { Text("不指定", style = MaterialTheme.typography.labelSmall) })
            Spacer(Modifier.height(4.dp))
            products.forEach { prod ->
                val isSel = selectedProductId == prod.id
                FilterChip(selected = isSel, onClick = { selectedProductId = prod.id }, label = { Text(prod.productName, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color.copy(alpha = 0.12f), selectedLabelColor = color))
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = amount, onValueChange = { if (it.isEmpty() || it.matches(Regex("^-?\\d*\\.?\\d{0,2}$"))) amount = it },
                label = { Text("收益金额") }, leadingIcon = { Text("¥") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            Spacer(Modifier.height(12.dp))
            Text("收益类型", style = MaterialTheme.typography.labelLarge, color = Gray700)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("分红", "利息", "资本利得", "其他").forEach { t ->
                    FilterChip(selected = incomeType == t, onClick = { incomeType = t }, label = { Text(t, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color.copy(alpha = 0.12f), selectedLabelColor = color))
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("日期") }, leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)

            Spacer(Modifier.height(24.dp))
            val amt = amount.toDoubleOrNull() ?: 0.0
            Button(onClick = {
                scope.launch {
                    repository.updateGrowthSubAccountIdle(subAccountId, amt)
                    repository.insertInvestmentIncome(InvestmentIncome(productId = selectedProductId ?: subAccountId, amount = amt, incomeType = incomeType, date = date, note = incomeType))
                    onSave()
                }
            }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = color), enabled = amt > 0) {
                Text("保存收益", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
