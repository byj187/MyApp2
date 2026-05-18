package com.anxincaiguan.ui.stable

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
import androidx.compose.ui.unit.sp
import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.data.model.*
import com.anxincaiguan.ui.theme.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toInstant
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StableDetailScreen(
    accountId: Long,
    repository: Repository,
    onBack: () -> Unit = {}
) {
    val viewModel = remember { StableDetailViewModel(repository, accountId) }
    val state by viewModel.uiState.collectAsState()
    var showBuyDialog by remember { mutableStateOf(false) }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showMatureDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadData() }

    StableDetailContent(
        state = state,
        onBack = onBack,
        onBuyClick = { showBuyDialog = true },
        onIncomeClick = { showIncomeDialog = true },
        onMatureClick = { showMatureDialog = true }
    )

    if (showBuyDialog) {
        BuyProductDialog(
            onDismiss = { showBuyDialog = false },
            onConfirm = { name, type, amount, rate, date, expire ->
                viewModel.buyProduct(name, type, amount, rate, date, expire) {
                    showBuyDialog = false
                }
            }
        )
    }

    if (showIncomeDialog) {
        RecordIncomeDialog(
            products = state.products,
            onDismiss = { showIncomeDialog = false },
            onConfirm = { productId, amount, date, note ->
                viewModel.recordIncome(productId, amount, date, note) {
                    showIncomeDialog = false
                }
            }
        )
    }

    if (showMatureDialog) {
        MatureDialog(
            products = state.products.filter { it.expireDate != null },
            onDismiss = { showMatureDialog = false },
            onConfirm = { product ->
                viewModel.redeemProduct(product) {
                    showMatureDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StableDetailContent(
    state: StableDetailUiState,
    onBack: () -> Unit,
    onBuyClick: () -> Unit,
    onIncomeClick: () -> Unit,
    onMatureClick: () -> Unit
) {
    val balance = state.account?.balance ?: 0.0
    val moneyMarket = balance - state.totalLocked

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("🛡️ 稳健保底", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StableColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { StableHeader(balance = balance, moneyMarket = moneyMarket, locked = state.totalLocked, totalIncome = state.totalIncome) }

            item { MatureBanner(expiredCount = state.expiredProducts.size, onClick = onMatureClick) }

            item {
                ActionButtonRow(
                    onBuyClick = onBuyClick,
                    onIncomeClick = onIncomeClick,
                    onMatureClick = onMatureClick
                )
            }

            item {
                Text(
                    "持有产品 (${state.products.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            if (state.products.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = Gray300
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "暂无产品，您的资金默认为货币基金",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray400
                            )
                        }
                    }
                }
            } else {
                items(state.products, key = { it.id }) { product ->
                    StableProductCard(product = product)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StableHeader(
    balance: Double,
    moneyMarket: Double,
    locked: Double,
    totalIncome: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("总资产", style = MaterialTheme.typography.labelLarge, color = Gray500)
            Spacer(Modifier.height(6.dp))
            Text(
                "¥${fmt2(balance)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("货基余额", "¥${fmt2(moneyMarket)}", StableColor)
                StatItem("已锁定", "¥${fmt2(locked)}", Gray600)
                StatItem("累计收益", "¥${fmt2(totalIncome)}", SuccessColor)
            }

            if (locked > 0) {
                Spacer(Modifier.height(16.dp))
                val ratio = (locked / balance).toFloat().coerceIn(0f, 1f)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("配置比例", style = MaterialTheme.typography.labelSmall, color = Gray500)
                        Text(
                            "${fmt1(locked / balance * 100)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StableColor
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = StableColor,
                        trackColor = Gray200
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MatureBanner(expiredCount: Int, onClick: () -> Unit) {
    if (expiredCount > 0) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WarningColor.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = WarningColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "有 $expiredCount 个产品已到期，请处理",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = WarningColor,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = WarningColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionButtonRow(
    onBuyClick: () -> Unit,
    onIncomeClick: () -> Unit,
    onMatureClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionButton(
            label = "买入产品",
            icon = Icons.Default.AddCircle,
            color = StableColor,
            modifier = Modifier.weight(1f),
            onClick = onBuyClick
        )
        ActionButton(
            label = "记录收益",
            icon = Icons.Default.TrendingUp,
            color = SuccessColor,
            modifier = Modifier.weight(1f),
            onClick = onIncomeClick
        )
        ActionButton(
            label = "到期处理",
            icon = Icons.Default.EventNote,
            color = WarningColor,
            modifier = Modifier.weight(1f),
            onClick = onMatureClick
        )
    }
}

@Composable
private fun RowScope.ActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StableProductCard(product: StableProduct) {
    val type = StableProductType.fromName(product.productType)
    val color = when (type) {
        StableProductType.TIME_DEPOSIT -> StableColor
        StableProductType.GOVERNMENT_BOND -> Blue600
        StableProductType.MONEY_MARKET -> SuccessColor
        StableProductType.STRUCTURED_DEPOSIT -> Orange500
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        type.emoji,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.productName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            type.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                        if (product.annualRate > 0) {
                            Text(
                                " · ${fmt1(product.annualRate * 100)}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "¥${fmt2(product.amount)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Gray100)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip("购买日", product.purchaseDate)
                if (product.expireDate != null) {
                    val daysLeft = daysBetween(product.expireDate!!, "2026-05-07")
                    val badgeColor = if (daysLeft <= 30) WarningColor else Gray500
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            product.expireDate!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray500
                        )
                        Spacer(Modifier.width(6.dp))
                        if (daysLeft >= 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "${daysLeft}天",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ErrorColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "已到期",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = ErrorColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$label ",
            style = MaterialTheme.typography.labelSmall,
            color = Gray400
        )
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            color = Gray600
        )
    }
}

private fun daysBetween(dateStr: String, todayStr: String): Int {
    return try {
        val parts = dateStr.split("-")
        val today = todayStr.split("-")
        val d = parts[0].toInt() * 365 + parts[1].toInt() * 30 + parts[2].toInt()
        val t = today[0].toInt() * 365 + today[1].toInt() * 30 + today[2].toInt()
        d - t
    } catch (_: Exception) { 0 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuyProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, amount: Double, rate: Double, date: String, expire: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(StableProductType.TIME_DEPOSIT) }
    var amount by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-05-07") }
    var expire by remember { mutableStateOf("") }
    var showTypeMenu by remember { mutableStateOf(false) }

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
            Text("买入产品", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("产品名称") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = showTypeMenu,
                onExpandedChange = { showTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("产品类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false }
                ) {
                    StableProductType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${type.emoji} ${type.displayName}")
                                }
                            },
                            onClick = {
                                selectedType = type
                                showTypeMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                label = { Text("金额") },
                leadingIcon = { Text("¥", style = MaterialTheme.typography.titleLarge) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = rate,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,4}$"))) rate = it },
                label = { Text("年化利率 (%)") },
                leadingIcon = { Text("%", style = MaterialTheme.typography.titleLarge) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(12.dp))
            DatePickerField(
                label = "购买日期",
                date = date,
                onDateSelected = { date = it }
            )

            if (selectedType != StableProductType.MONEY_MARKET) {
                Spacer(Modifier.height(12.dp))
                DatePickerField(
                    label = "到期日",
                    date = expire,
                    onDateSelected = { expire = it }
                )
            }

            Spacer(Modifier.height(24.dp))
            val amt = amount.toDoubleOrNull() ?: 0.0
            val rateVal = rate.toDoubleOrNull() ?: 0.0
            val canSave = name.isNotBlank() && amt > 0
            Button(
                onClick = { onConfirm(name, selectedType.name, amt, rateVal, date, expire.ifEmpty { null }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StableColor),
                enabled = canSave
            ) {
                Text("确认买入", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordIncomeDialog(
    products: List<StableProduct>,
    onDismiss: () -> Unit,
    onConfirm: (productId: Long, amount: Double, date: String, note: String) -> Unit
) {
    var selectedProductId by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-05-07") }
    var note by remember { mutableStateOf("") }

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
            Text("记录收益", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            Text("关联产品", style = MaterialTheme.typography.labelLarge, color = Gray700)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                products.forEach { product ->
                    val type = StableProductType.fromName(product.productType)
                    val isSel = selectedProductId == product.id
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedProductId = product.id },
                        label = {
                            Text(
                                "${type.emoji} ${product.productName}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StableColor.copy(alpha = 0.12f),
                            selectedLabelColor = StableColor
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                label = { Text("收益金额") },
                leadingIcon = { Text("¥", style = MaterialTheme.typography.titleLarge) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(12.dp))
            DatePickerField(
                label = "日期",
                date = date,
                onDateSelected = { date = it }
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（选填）") },
                leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))
            val amt = amount.toDoubleOrNull() ?: 0.0
            Button(
                onClick = { selectedProductId?.let { onConfirm(it, amt, date, note) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StableColor),
                enabled = selectedProductId != null && amt > 0
            ) {
                Text("保存收益", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatureDialog(
    products: List<StableProduct>,
    onDismiss: () -> Unit,
    onConfirm: (StableProduct) -> Unit
) {
    val today = "2026-05-07"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("到期处理", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                if (products.isEmpty()) {
                    Text("暂无到期的产品", style = MaterialTheme.typography.bodyMedium, color = Gray500)
                } else {
                    products.forEach { product ->
                        val type = StableProductType.fromName(product.productType)
                        val isExpired = product.expireDate != null && product.expireDate!! <= today
                        val cardColor = if (isExpired) ErrorColor.copy(alpha = 0.06f) else SurfaceLight

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onConfirm(product) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(type.emoji, fontSize = 24.sp)
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        product.productName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "到期日: ${product.expireDate ?: "未知"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Gray500
                                    )
                                }
                                Text(
                                    "处理",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Blue600
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    date: String,
    onDateSelected: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = date,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.EditCalendar, contentDescription = "选择日期")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true },
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )

    if (showPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseDateToMillis(date)
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(formatMillisToDate(millis))
                        }
                        showPicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun parseDateToMillis(date: String): Long? {
    return try {
        val localDate = date.toLocalDate()
        val localDateTime = localDate.atTime(0, 0, 0)
        localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    } catch (_: Exception) { null }
}

private fun formatMillisToDate(millis: Long): String {
    return try {
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
        val zonedDateTime = instant.atZone(TimeZone.currentSystemDefault())
        val localDate = zonedDateTime.date
        "${localDate.year}-${localDate.monthNumber.toString().padStart(2, '0')}-${localDate.dayOfMonth.toString().padStart(2, '0')}"
    } catch (_: Exception) {
        "1970-01-01"
    }
}
