package com.anxincaiguan.ui.account

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anxincaiguan.data.local.Repository
import com.anxincaiguan.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    repository: Repository,
    onBack: () -> Unit = {}
) {
    val viewModel = remember { TransferViewModel(repository) }
    val state by viewModel.uiState.collectAsState()
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val allAccounts = viewModel.allAccounts()

    LaunchedEffect(Unit) { viewModel.loadAccounts() }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) showSuccessDialog = true
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false; onBack() },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessColor) },
            title = { Text(text = "调拨记录已保存", fontWeight = FontWeight.SemiBold) },
            text = { Text(text = "请自行完成实际资金划转。") },
            confirmButton = { Button(onClick = { showSuccessDialog = false; onBack() }) { Text(text = "确定") } }
        )
    }

    if (state.showInsufficientDialog) {
        val alertText: String = "转出账户余额（¥" + fmt2(state.fromOption?.availableBalance ?: 0.0) + "）不足以完成本次调拨，是否仍要保存？"
        AlertDialog(
            onDismissRequest = { viewModel.dismissInsufficientDialog() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = WarningColor) },
            title = { Text(text = "余额不足", fontWeight = FontWeight.SemiBold) },
            text = { Text(text = alertText) },
            confirmButton = {
                Button(onClick = { viewModel.confirmSave() }, colors = ButtonDefaults.buttonColors(containerColor = WarningColor)) {
                    Text(text = "仍要保存")
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.dismissInsufficientDialog() }) { Text(text = "取消") } }
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text(text = "资金调拨", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AccountSelectorCard(
                    label = "转出账户",
                    selected = state.fromOption,
                    onClick = { showFromPicker = true },
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Gray400, modifier = Modifier.size(24.dp))
                AccountSelectorCard(
                    label = "转入账户",
                    selected = state.toOption,
                    onClick = { showToPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.setAmount(it) },
                label = { Text(text = "调拨金额") },
                leadingIcon = { Text(text = "¥", style = MaterialTheme.typography.titleLarge) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.date,
                onValueChange = { viewModel.setDate(it) },
                label = { Text(text = "日期") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.note,
                onValueChange = { viewModel.setNote(it) },
                label = { Text(text = "备注（选填）") },
                leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            val amountVal = state.amount.toDoubleOrNull() ?: 0.0
            if (state.fromOption != null && state.toOption != null && amountVal > 0) {
                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Blue50)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "调拨摘要", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Blue600)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val fromEmoji: String = state.fromOption!!.emoji
                            val fromName: String = state.fromOption!!.displayName
                            val toEmoji: String = state.toOption!!.emoji
                            val toName: String = state.toOption!!.displayName
                            Text(text = fromEmoji, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                            Spacer(Modifier.width(6.dp))
                            Text(text = fromName, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(6.dp))
                            Text(text = "\u2192", color = Gray400)
                            Spacer(Modifier.width(6.dp))
                            Text(text = toEmoji, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                            Spacer(Modifier.width(6.dp))
                            Text(text = toName, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row {
                            Text(text = "金额：", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            val amountText: String = "¥" + fmt2(amountVal)
                            Text(text = amountText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Blue600)
                        }
                        if (state.fromOption!!.availableBalance < amountVal) {
                            Spacer(Modifier.height(4.dp))
                            val balWarnText: String = "\u26A0 余额不足：可用 ¥" + fmt2(state.fromOption!!.availableBalance)
                            Text(text = balWarnText, style = MaterialTheme.typography.labelSmall, color = WarningColor)
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(16.dp))

            val canSave = state.fromOption != null && state.toOption != null
                    && amountVal > 0 && !state.isSaving
                    && !(state.fromOption?.id == state.toOption?.id && state.fromOption?.type == state.toOption?.type)

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                enabled = canSave
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(text = "确认调拨", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showFromPicker) {
        AccountPickerDialog(
            title = "选择转出账户",
            accounts = allAccounts,
            onDismiss = { showFromPicker = false },
            onSelect = { viewModel.setFrom(it); showFromPicker = false }
        )
    }

    if (showToPicker) {
        AccountPickerDialog(
            title = "选择转入账户",
            accounts = allAccounts,
            onDismiss = { showToPicker = false },
            onSelect = { viewModel.setTo(it); showToPicker = false }
        )
    }
}

@Composable
private fun AccountSelectorCard(
    label: String,
    selected: TransferAccountOption?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Gray500)
            Spacer(Modifier.height(8.dp))
            if (selected != null) {
                val selEmoji: String = selected.emoji
                val selName: String = selected.displayName
                val selBal: String = "可用 ¥" + fmt2(selected.availableBalance)
                Text(text = selEmoji, fontSize = 28.sp)
                Spacer(Modifier.height(4.dp))
                Text(text = selName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(text = selBal, style = MaterialTheme.typography.labelSmall, color = Gray500)
            } else {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(32.dp), tint = Gray400)
                Spacer(Modifier.height(4.dp))
                Text(text = "选择账户", style = MaterialTheme.typography.labelMedium, color = Gray400)
            }
        }
    }
}

@Composable
private fun AccountPickerDialog(
    title: String,
    accounts: List<TransferAccountOption>,
    onDismiss: () -> Unit,
    onSelect: (TransferAccountOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                accounts.forEach { option ->
                    val optName: String = option.displayName
                    val optEmoji: String = option.emoji
                    val optPrefix: String = if (option is TransferAccountOption.Main) "余额" else "闲置"
                    val optBal: String = optPrefix + " ¥" + fmt2(option.availableBalance)
                    ListItem(
                        headlineContent = { Text(text = optName, fontWeight = FontWeight.Medium) },
                        leadingContent = { Text(text = optEmoji, fontSize = 20.sp) },
                        supportingContent = { Text(text = optBal, color = Gray500) },
                        modifier = Modifier
                            .clickable { onSelect(option) }
                            .padding(vertical = 2.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = "取消") } }
    )
}
