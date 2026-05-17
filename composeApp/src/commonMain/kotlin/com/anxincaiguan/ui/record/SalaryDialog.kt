package com.anxincaiguan.ui.record

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
import kotlinx.serialization.json.*

data class SalaryUiState(
    val totalSalary: String = "",
    val dailyFixedAmount: Double = 0.0,
    val ratios: Map<String, Double> = emptyMap(),
    val qualityAmount: String = "",
    val stableAmount: String = "",
    val growthAmount: String = "",
    val mainAccounts: List<MainAccount> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val growthAccountBalanceDelta: Double = 0.0,
    val showResultDialog: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryDialog(
    repository: Repository,
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf(SalaryUiState()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val settings = repository.getSettings()
        val accounts = repository.getAllMainAccounts()
        if (settings != null) {
            val json = Json { ignoreUnknownKeys = true }
            val ratiosObj = try {
                json.decodeFromString<JsonObject>(settings.mainRatiosForSalary)
            } catch (_: Exception) { JsonObject(emptyMap()) }
            val ratios = mapOf(
                MainAccountType.QUALITY.name to (ratiosObj[MainAccountType.QUALITY.name]?.jsonPrimitive?.doubleOrNull ?: 0.3),
                MainAccountType.STABLE.name to (ratiosObj[MainAccountType.STABLE.name]?.jsonPrimitive?.doubleOrNull ?: 0.4),
                MainAccountType.GROWTH.name to (ratiosObj[MainAccountType.GROWTH.name]?.jsonPrimitive?.doubleOrNull ?: 0.3)
            )
            state = state.copy(
                dailyFixedAmount = settings.dailyFixedAmount,
                ratios = ratios,
                mainAccounts = accounts
            )
        }
        loaded = true
    }

    val total = state.totalSalary.toDoubleOrNull() ?: 0.0
    val dailyFixed = state.dailyFixedAmount
    val remaining = (total - dailyFixed).coerceAtLeast(0.0)

    val qualityVal = state.qualityAmount.toDoubleOrNull() ?: (remaining * (state.ratios[MainAccountType.QUALITY.name] ?: 0.3))
    val stableVal = state.stableAmount.toDoubleOrNull() ?: (remaining * (state.ratios[MainAccountType.STABLE.name] ?: 0.4))
    val growthVal = state.growthAmount.toDoubleOrNull() ?: (remaining * (state.ratios[MainAccountType.GROWTH.name] ?: 0.3))

    val sumOther = qualityVal + stableVal + growthVal
    val isBalanced = kotlin.math.abs(sumOther - remaining) < 0.01
    val canSave = total > 0 && remaining >= 0 && isBalanced && !state.isSaving

    fun recalcFromRatios() {
        val r = (state.totalSalary.toDoubleOrNull() ?: 0.0) - state.dailyFixedAmount
        if (r > 0) {
            val qRatio = state.ratios[MainAccountType.QUALITY.name] ?: 0.3
            val sRatio = state.ratios[MainAccountType.STABLE.name] ?: 0.4
            val gRatio = state.ratios[MainAccountType.GROWTH.name] ?: 0.3
            state = state.copy(
                qualityAmount = fmt2(r * qRatio),
                stableAmount = fmt2(r * sRatio),
                growthAmount = fmt2(r * gRatio)
            )
        } else {
            state = state.copy(qualityAmount = "", stableAmount = "", growthAmount = "")
        }
    }

    LaunchedEffect(state.totalSalary) {
        if (loaded) recalcFromRatios()
    }

    fun doSave() {
        scope.launch {
            state = state.copy(isSaving = true)
            try {
                val accounts = state.mainAccounts
                val date = "2026-05-07"

                val dailyAcct = accounts.find { it.name == MainAccountType.DAILY.displayName }
                val qualityAcct = accounts.find { it.name == MainAccountType.QUALITY.displayName }
                val stableAcct = accounts.find { it.name == MainAccountType.STABLE.displayName }
                val growthAcct = accounts.find { it.name == MainAccountType.GROWTH.displayName }

                val allocations = mapOf(
                    MainAccountType.QUALITY.name to qualityVal,
                    MainAccountType.STABLE.name to stableVal,
                    MainAccountType.GROWTH.name to growthVal
                )
                val allocationJsonStr = buildJsonObject {
                    put(MainAccountType.QUALITY.name, qualityVal)
                    put(MainAccountType.STABLE.name, stableVal)
                    put(MainAccountType.GROWTH.name, growthVal)
                }.toString()

                // Bill for daily (fixed amount)
                dailyAcct?.let { acct ->
                    repository.insertBill(Bill(accountType = "main", accountId = acct.id, type = "INCOME", amount = dailyFixed, category = "日常消费(工资)", date = date, note = "工资-日常固定"))
                    repository.updateMainAccountBalance(acct.id, dailyFixed)
                }
                // Bill for quality
                qualityAcct?.let { acct ->
                    repository.insertBill(Bill(accountType = "main", accountId = acct.id, type = "INCOME", amount = qualityVal, category = "生活品质(工资)", date = date, note = "工资-按比例分配"))
                    repository.updateMainAccountBalance(acct.id, qualityVal)
                }
                // Bill for stable
                stableAcct?.let { acct ->
                    repository.insertBill(Bill(accountType = "main", accountId = acct.id, type = "INCOME", amount = stableVal, category = "稳健保底(工资)", date = date, note = "工资-按比例分配"))
                    repository.updateMainAccountBalance(acct.id, stableVal)
                }
                // Bill for growth
                growthAcct?.let { acct ->
                    repository.insertBill(Bill(accountType = "main", accountId = acct.id, type = "INCOME", amount = growthVal, category = "增值投资(工资)", date = date, note = "工资-按比例分配"))
                    repository.updateMainAccountBalance(acct.id, growthVal)
                }

                // Salary record
                repository.insertSalaryRecord(
                    SalaryRecord(total = total, date = date, dailyFixed = dailyFixed, remaining = remaining, allocationJson = allocationJsonStr)
                )

                state = state.copy(isSaving = false, saveSuccess = true, growthAccountBalanceDelta = growthVal, showResultDialog = true)
            } catch (_: Exception) {
                state = state.copy(isSaving = false)
            }
        }
    }

    if (state.showResultDialog) {
        AlertDialog(
            onDismissRequest = {
                state = state.copy(showResultDialog = false)
                onSaveSuccess()
                onDismiss()
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(36.dp)) },
            title = { Text("工资分配完成", fontWeight = FontWeight.SemiBold) },
            text = {
                Text("增值投资账户已入账 ¥${fmt2(state.growthAccountBalanceDelta)}，请前往『增值投资』页面分配至子账户。")
            },
            confirmButton = {
                Button(onClick = {
                    state = state.copy(showResultDialog = false)
                    onSaveSuccess()
                    onDismiss()
                }) { Text("确定") }
            }
        )
        return
    }

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
            if (!loaded) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            // Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Blue50),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Blue600, modifier = Modifier.size(22.dp)) }
                Spacer(Modifier.width(12.dp))
                Text("工资分配", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(20.dp))

            // Total Salary Input
            OutlinedTextField(
                value = state.totalSalary,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) state = state.copy(totalSalary = it) },
                label = { Text("税后工资金额") },
                leadingIcon = { Text("¥", style = MaterialTheme.typography.titleLarge) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(20.dp))

            // Allocation Table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Gray50),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("分配账户", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelSmall, color = Gray500)
                        Text("比例", modifier = Modifier.weight(0.7f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = Gray500)
                        Text("金额", modifier = Modifier.weight(1f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = Gray500)
                    }

                    HorizontalDivider(color = Gray200)

                    // Daily (read-only)
                    val dailyRatioStr = "固定"
                    AllocationRow(
                        emoji = "💳",
                        label = "日常消费",
                        ratio = dailyRatioStr,
                        amount = "¥${fmt2(dailyFixed)}",
                        amountColor = Gray800,
                        isReadOnly = true
                    )

                    HorizontalDivider(color = Gray200, modifier = Modifier.padding(vertical = 2.dp))

                    // Quality (editable)
                    val qualityRatioPct = "${((state.ratios[MainAccountType.QUALITY.name] ?: 0.0) * 100).toInt()}%"
                    AllocationRow(
                        emoji = "🎭",
                        label = "生活品质",
                        ratio = qualityRatioPct,
                        amount = state.qualityAmount,
                        amountColor = QualityColor,
                        isReadOnly = false,
                        onAmountChange = { state = state.copy(qualityAmount = it) }
                    )

                    HorizontalDivider(color = Gray200, modifier = Modifier.padding(vertical = 2.dp))

                    // Stable (editable)
                    val stableRatioPct = "${((state.ratios[MainAccountType.STABLE.name] ?: 0.0) * 100).toInt()}%"
                    AllocationRow(
                        emoji = "🛡️",
                        label = "稳健保底",
                        ratio = stableRatioPct,
                        amount = state.stableAmount,
                        amountColor = StableColor,
                        isReadOnly = false,
                        onAmountChange = { state = state.copy(stableAmount = it) }
                    )

                    HorizontalDivider(color = Gray200, modifier = Modifier.padding(vertical = 2.dp))

                    // Growth (editable)
                    val growthRatioPct = "${((state.ratios[MainAccountType.GROWTH.name] ?: 0.0) * 100).toInt()}%"
                    AllocationRow(
                        emoji = "📈",
                        label = "增值投资",
                        ratio = growthRatioPct,
                        amount = state.growthAmount,
                        amountColor = GrowthColor,
                        isReadOnly = false,
                        onAmountChange = { state = state.copy(growthAmount = it) }
                    )

                    HorizontalDivider(color = Gray200, modifier = Modifier.padding(vertical = 4.dp))

                    // Summary
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("总额", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (total > 0) {
                                Text("日常 ¥${fmt2(dailyFixed)} + 剩余 ¥${fmt2(remaining)}", style = MaterialTheme.typography.labelSmall, color = Gray500)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("¥${fmt2(total)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Blue600)
                            if (total > 0) {
                                Text(
                                    if (isBalanced) "✓ 已平衡" else "剩余: ¥${fmt2(remaining)} · 分配: ¥${fmt2(sumOther)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isBalanced) SuccessColor else ErrorColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = { doSave() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                enabled = canSave
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("保存分配", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun AllocationRow(
    emoji: String,
    label: String,
    ratio: String,
    amount: String,
    amountColor: Color,
    isReadOnly: Boolean,
    onAmountChange: ((String) -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label
        Row(modifier = Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }

        // Ratio
        Text(ratio, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = Gray500)

        // Amount
        if (isReadOnly) {
            Text(amount, modifier = Modifier.weight(1f), textAlign = TextAlign.End, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = amountColor)
        } else {
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) onAmountChange?.invoke(it) },
                modifier = Modifier.weight(1f).height(44.dp),
                textStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = amountColor,
                    unfocusedTextColor = amountColor,
                    focusedBorderColor = amountColor.copy(alpha = 0.5f),
                    unfocusedBorderColor = Gray200
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}
