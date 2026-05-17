package com.anxincaiguan.ui.onboarding

import androidx.compose.foundation.*

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.pager.HorizontalPager

import androidx.compose.foundation.pager.rememberPagerState

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp

import com.anxincaiguan.data.model.*

import com.anxincaiguan.ui.theme.*

import kotlinx.coroutines.launch

import kotlinx.serialization.encodeToString

import kotlinx.serialization.json.Json

import kotlin.math.roundToLong

@OptIn(ExperimentalFoundationApi::class)

@Composable

fun OnboardingScreen(

    onComplete: (

        mainAccounts: List<MainAccount>,

        growthSubAccounts: List<GrowthSubAccount>,

        settings: AppSettings

    ) -> Unit

) {

    val pagerState = rememberPagerState(pageCount = { 5 })

    val scope = rememberCoroutineScope()

    val json = remember { Json { ignoreUnknownKeys = true; encodeDefaults = true } }

    var dailyRatio by rememberSaveable { mutableFloatStateOf(20f) }

    var qualityRatio by rememberSaveable { mutableFloatStateOf(20f) }

    var stableRatio by rememberSaveable { mutableFloatStateOf(30f) }

    var growthRatio by rememberSaveable { mutableFloatStateOf(30f) }

    var dailyFixed by rememberSaveable { mutableFloatStateOf(3000f) }

    var salaryQualityRatio by rememberSaveable { mutableFloatStateOf(30f) }

    var salaryStableRatio by rememberSaveable { mutableFloatStateOf(20f) }

    var salaryGrowthRatio by rememberSaveable { mutableFloatStateOf(50f) }

    var indexFundRatio by rememberSaveable { mutableFloatStateOf(50f) }

    var activeEquityRatio by rememberSaveable { mutableFloatStateOf(30f) }

    var bondRatio by rememberSaveable { mutableFloatStateOf(15f) }

    var alternativeRatio by rememberSaveable { mutableFloatStateOf(5f) }

    var totalAssets by rememberSaveable { mutableStateOf("500000") }

    fun buildData(): Triple<List<MainAccount>, List<GrowthSubAccount>, AppSettings> {

        val assets = totalAssets.toDoubleOrNull() ?: 0.0

        val dR = dailyRatio.toDouble()

        val qR = qualityRatio.toDouble()

        val sR = stableRatio.toDouble()

        val gR = growthRatio.toDouble()

        val sqR = salaryQualityRatio.toDouble()

        val ssR = salaryStableRatio.toDouble()

        val sgR = salaryGrowthRatio.toDouble()

        val iR = indexFundRatio.toDouble()

        val aR = activeEquityRatio.toDouble()

        val bR = bondRatio.toDouble()

        val alR = alternativeRatio.toDouble()

        val mains = listOf(

            MainAccount(name = MainAccountType.DAILY.displayName, balance = assets * dR / 100.0, targetRatioForSalary = 0.0),

            MainAccount(name = MainAccountType.QUALITY.displayName, balance = assets * qR / 100.0, targetRatioForSalary = sqR / 100.0),

            MainAccount(name = MainAccountType.STABLE.displayName, balance = assets * sR / 100.0, targetRatioForSalary = ssR / 100.0),

            MainAccount(name = MainAccountType.GROWTH.displayName, balance = assets * gR / 100.0, targetRatioForSalary = sgR / 100.0)

        )

        val growthAsset = mains[3].balance

        val subs = listOf(

            GrowthSubAccount(name = GrowthSubAccountType.INDEX_FUND.displayName, idleAmount = growthAsset * iR / 100.0, investedAmount = 0.0, targetRatio = iR / 100.0),

            GrowthSubAccount(name = GrowthSubAccountType.ACTIVE_EQUITY.displayName, idleAmount = growthAsset * aR / 100.0, investedAmount = 0.0, targetRatio = aR / 100.0),

            GrowthSubAccount(name = GrowthSubAccountType.BOND_FIXED_INCOME.displayName, idleAmount = growthAsset * bR / 100.0, investedAmount = 0.0, targetRatio = bR / 100.0),

            GrowthSubAccount(name = GrowthSubAccountType.OTHER_ALTERNATIVE.displayName, idleAmount = growthAsset * alR / 100.0, investedAmount = 0.0, targetRatio = alR / 100.0)

        )

        val settings = AppSettings(

            mainRatiosForSalary = json.encodeToString(mapOf(

                MainAccountType.DAILY.name to 0.0,

                MainAccountType.QUALITY.name to sqR / 100.0,

                MainAccountType.STABLE.name to ssR / 100.0,

                MainAccountType.GROWTH.name to sgR / 100.0

            )),

            growthRatios = json.encodeToString(mapOf(

                GrowthSubAccountType.INDEX_FUND.name to iR / 100.0,

                GrowthSubAccountType.ACTIVE_EQUITY.name to aR / 100.0,

                GrowthSubAccountType.BOND_FIXED_INCOME.name to bR / 100.0,

                GrowthSubAccountType.OTHER_ALTERNATIVE.name to alR / 100.0

            )),

            deviationThreshold = 0.05,

            dailyFixedAmount = dailyFixed.toDouble(),

            stockAssetRatios = json.encodeToString(mapOf(

                MainAccountType.DAILY.name to dR / 100.0,

                MainAccountType.QUALITY.name to qR / 100.0,

                MainAccountType.STABLE.name to sR / 100.0,

                MainAccountType.GROWTH.name to gR / 100.0

            ))

        )

        return Triple(mains, subs, settings)

    }

    Scaffold(containerColor = BackgroundLight) { padding ->

        Column(

            modifier = Modifier.fillMaxSize().padding(padding)

        ) {

            Row(

                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),

                verticalAlignment = Alignment.CenterVertically

            ) {

                if (pagerState.currentPage > 0) {

                    IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }) {

                        Icon(Icons.Default.ArrowBack, contentDescription = "上一步", tint = Gray600)

                    }

                } else {

                    Spacer(Modifier.size(48.dp))

                }

                Spacer(Modifier.weight(1f))

                if (pagerState.currentPage < 4) {

                    TextButton(onClick = { scope.launch { pagerState.animateScrollToPage(4) } }) {

                        Text("跳过", color = Gray500)

                    }

                }

            }

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.Center,

                verticalAlignment = Alignment.CenterVertically

            ) {

                repeat(5) { index ->

                    Box(

                        modifier = Modifier

                            .padding(horizontal = 3.dp)

                            .size(if (index == pagerState.currentPage) 32.dp else 8.dp, 8.dp)

                            .clip(CircleShape)

                            .background(if (index <= pagerState.currentPage) Blue600 else Gray300)

                    )

                }

            }

            Text(

                "第${pagerState.currentPage + 1} 步，共5 步",

                style = MaterialTheme.typography.labelMedium,

                color = Gray500,

                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),

                textAlign = TextAlign.Center

            )

            HorizontalPager(

                state = pagerState,

                modifier = Modifier.weight(1f).fillMaxWidth(),

                userScrollEnabled = false

            ) { page ->

                Box(

                    modifier = Modifier

                        .fillMaxSize()

                        .verticalScroll(rememberScrollState())

                        .padding(horizontal = 24.dp)

                ) {

                    when (page) {

                        0 -> Step1MainRatios(

                            dailyRatio = dailyRatio, qualityRatio = qualityRatio,

                            stableRatio = stableRatio, growthRatio = growthRatio,

                            onDailyChange = { v -> val r = 100f - qualityRatio - stableRatio - growthRatio; dailyRatio = v.coerceIn(0f, r) },

                            onQualityChange = { v -> val r = 100f - dailyRatio - stableRatio - growthRatio; qualityRatio = v.coerceIn(0f, r) },

                            onStableChange = { v -> val r = 100f - dailyRatio - qualityRatio - growthRatio; stableRatio = v.coerceIn(0f, r) },

                            onGrowthChange = { v -> val r = 100f - dailyRatio - qualityRatio - stableRatio; growthRatio = v.coerceIn(0f, r) },

                            onNext = { if (dailyRatio + qualityRatio + stableRatio + growthRatio == 100f) scope.launch { pagerState.animateScrollToPage(1) } }

                        )

                        1 -> Step2SalaryRules(

                            dailyFixed = dailyFixed, qualityRatio = salaryQualityRatio,

                            stableRatio = salaryStableRatio, growthRatio = salaryGrowthRatio,

                            onDailyFixedChange = { dailyFixed = it },

                            onQualityChange = { v -> val r = 100f - salaryStableRatio - salaryGrowthRatio; salaryQualityRatio = v.coerceIn(0f, r) },

                            onStableChange = { v -> val r = 100f - salaryQualityRatio - salaryGrowthRatio; salaryStableRatio = v.coerceIn(0f, r) },

                            onGrowthChange = { v -> val r = 100f - salaryQualityRatio - salaryStableRatio; salaryGrowthRatio = v.coerceIn(0f, r) },

                            onNext = { scope.launch { pagerState.animateScrollToPage(2) } }

                        )

                        2 -> Step3SubAccountRatios(

                            indexFundRatio = indexFundRatio, activeEquityRatio = activeEquityRatio,

                            bondRatio = bondRatio, alternativeRatio = alternativeRatio,

                            onIndexChange = { v -> val r = 100f - activeEquityRatio - bondRatio - alternativeRatio; indexFundRatio = v.coerceIn(0f, r) },

                            onActiveChange = { v -> val r = 100f - indexFundRatio - bondRatio - alternativeRatio; activeEquityRatio = v.coerceIn(0f, r) },

                            onBondChange = { v -> val r = 100f - indexFundRatio - activeEquityRatio - alternativeRatio; bondRatio = v.coerceIn(0f, r) },

                            onAlternativeChange = { v -> val r = 100f - indexFundRatio - activeEquityRatio - bondRatio; alternativeRatio = v.coerceIn(0f, r) },

                            onNext = { scope.launch { pagerState.animateScrollToPage(3) } }

                        )

                        3 -> Step4AssetAllocation(

                            totalAssets = totalAssets, onTotalAssetsChange = { totalAssets = it },

                            dailyRatio = dailyRatio, qualityRatio = qualityRatio,

                            stableRatio = stableRatio, growthRatio = growthRatio,

                            onNext = { scope.launch { pagerState.animateScrollToPage(4) } }

                        )

                        4 -> Step5Complete(

                            growthBalance = (totalAssets.toDoubleOrNull() ?: 0.0) * (growthRatio / 100f),

                            onAutoAllocate = { val (m, s, st) = buildData(); onComplete(m, s, st) },

                            onManualLater = { val (m, s, st) = buildData(); onComplete(m, s, st) }

                        )

                    }

                }

            }

        }

    }

}

// ─── Step 1 ────────────────────────────────────────────────────────────────

@Composable

private fun Step1MainRatios(

    dailyRatio: Float, qualityRatio: Float, stableRatio: Float, growthRatio: Float,

    onDailyChange: (Float) -> Unit, onQualityChange: (Float) -> Unit,

    onStableChange: (Float) -> Unit, onGrowthChange: (Float) -> Unit,

    onNext: () -> Unit

) {

    val total = dailyRatio + qualityRatio + stableRatio + growthRatio

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Blue50), contentAlignment = Alignment.Center) {

            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(40.dp), tint = Blue600)

        }

        Spacer(Modifier.height(16.dp))

        Text("设定四大账户比例", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Text("为您的资产设定分配比例", style = MaterialTheme.typography.bodyMedium, color = Gray500)

        Spacer(Modifier.height(8.dp))

        Text("总和：${total.roundToLong()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (total == 100f) SuccessColor else ErrorColor)

        Spacer(Modifier.height(16.dp))

        RatioSlider("日常消费", "💳", dailyRatio, DailyColor, onDailyChange)

        RatioSlider("生活品质", "🎭", qualityRatio, QualityColor, onQualityChange)

        RatioSlider("稳健保底", "🛡", stableRatio, StableColor, onStableChange)

        RatioSlider("增值投资", "📈", growthRatio, GrowthColor, onGrowthChange)

        Spacer(Modifier.height(24.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600), enabled = total == 100f) {

            Text("下一步", style = MaterialTheme.typography.titleMedium)

        }

        Spacer(Modifier.height(32.dp))

    }

}

// ─── Step 2 ────────────────────────────────────────────────────────────────

@Composable

private fun Step2SalaryRules(

    dailyFixed: Float, qualityRatio: Float, stableRatio: Float, growthRatio: Float,

    onDailyFixedChange: (Float) -> Unit,

    onQualityChange: (Float) -> Unit, onStableChange: (Float) -> Unit, onGrowthChange: (Float) -> Unit,

    onNext: () -> Unit

) {

    val total = qualityRatio + stableRatio + growthRatio

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Orange50), contentAlignment = Alignment.Center) {

            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(40.dp), tint = Orange500)

        }

        Spacer(Modifier.height(16.dp))

        Text("工资分配规则", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Text("每月工资自动按规则分配", style = MaterialTheme.typography.bodyMedium, color = Gray500)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(

            value = fmt0(dailyFixed.toDouble()),

            onValueChange = { it.toFloatOrNull()?.let(onDailyFixedChange) },

            label = { Text("日常消费固定金额") },

            leadingIcon = { Text("¥", style = MaterialTheme.typography.titleMedium) },

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(12.dp), singleLine = true,

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

        )

        Spacer(Modifier.height(12.dp))

        Text("剩余部分按比例分配", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Gray700)

        Text("剩余总和：${total.roundToLong()}%", style = MaterialTheme.typography.labelMedium, color = if (total == 100f) SuccessColor else ErrorColor)

        Spacer(Modifier.height(12.dp))

        RatioSlider("生活品质", "🎭", qualityRatio, QualityColor, onQualityChange)

        RatioSlider("稳健保底", "🛡", stableRatio, StableColor, onStableChange)

        RatioSlider("增值投资", "📈", growthRatio, GrowthColor, onGrowthChange)

        Spacer(Modifier.height(24.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600), enabled = total == 100f) {

            Text("下一步", style = MaterialTheme.typography.titleMedium)

        }

        Spacer(Modifier.height(32.dp))

    }

}

// ─── Step 3 ────────────────────────────────────────────────────────────────

@Composable

private fun Step3SubAccountRatios(

    indexFundRatio: Float, activeEquityRatio: Float, bondRatio: Float, alternativeRatio: Float,

    onIndexChange: (Float) -> Unit, onActiveChange: (Float) -> Unit,

    onBondChange: (Float) -> Unit, onAlternativeChange: (Float) -> Unit,

    onNext: () -> Unit

) {

    val total = indexFundRatio + activeEquityRatio + bondRatio + alternativeRatio

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(GrowthColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {

            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(40.dp), tint = GrowthColor)

        }

        Spacer(Modifier.height(16.dp))

        Text("增值投资子账户比例", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Text("设定增值投资的内部子账户比例", style = MaterialTheme.typography.bodyMedium, color = Gray500)

        Text("总和：${total.roundToLong()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (total == 100f) SuccessColor else ErrorColor)

        Spacer(Modifier.height(16.dp))

        RatioSlider("指数基金", "📊", indexFundRatio, IndexFundColor, onIndexChange)

        RatioSlider("主动权益基金", "🎯", activeEquityRatio, ActiveEquityColor, onActiveChange)

        RatioSlider("债券/固收", "🔒", bondRatio, BondColor, onBondChange)

        RatioSlider("其他另类", "💎", alternativeRatio, AlternativeColor, onAlternativeChange)

        Spacer(Modifier.height(24.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600), enabled = total == 100f) {

            Text("下一步", style = MaterialTheme.typography.titleMedium)

        }

        Spacer(Modifier.height(32.dp))

    }

}

// ─── Step 4 ────────────────────────────────────────────────────────────────

@Composable

private fun Step4AssetAllocation(

    totalAssets: String, onTotalAssetsChange: (String) -> Unit,

    dailyRatio: Float, qualityRatio: Float, stableRatio: Float, growthRatio: Float,

    onNext: () -> Unit

) {

    val assets = totalAssets.toDoubleOrNull() ?: 0.0

    val dailyAmount = assets * dailyRatio.toDouble() / 100.0

    val qualityAmount = assets * qualityRatio.toDouble() / 100.0

    val stableAmount = assets * stableRatio.toDouble() / 100.0

    val growthAmount = assets * growthRatio.toDouble() / 100.0

    val calculatedTotal = dailyAmount + qualityAmount + stableAmount + growthAmount

    val isValid = assets > 0 && kotlin.math.abs(calculatedTotal - assets) < 0.01

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(StableColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {

            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(40.dp), tint = StableColor)

        }

        Spacer(Modifier.height(16.dp))

        Text("存量资产录入", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(

            value = totalAssets, onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) onTotalAssetsChange(it) },

            label = { Text("总资产金额") }, leadingIcon = { Text("¥", style = MaterialTheme.typography.titleMedium) },

            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)

        )

        Spacer(Modifier.height(20.dp))

        if (assets > 0) {

            Card(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

                colors = CardDefaults.cardColors(containerColor = SurfaceLight)

            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Text("分配预览", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Gray700)

                    Spacer(Modifier.height(8.dp))

                    AllocationRow("💳 日常消费", dailyRatio, dailyAmount, DailyColor)

                    AllocationRow("🎭 生活品质", qualityRatio, qualityAmount, QualityColor)

                    AllocationRow("🛡 稳健保底", stableRatio, stableAmount, StableColor)

                    AllocationRow("📈 增值投资", growthRatio, growthAmount, GrowthColor)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                        Text("合计", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        Text("¥" + fmt2(calculatedTotal), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    }

                }

            }

        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600), enabled = isValid) {

            Text("下一步", style = MaterialTheme.typography.titleMedium)

        }

        Spacer(Modifier.height(32.dp))

    }

}

// ─── Step 5 ────────────────────────────────────────────────────────────────

@Composable

private fun Step5Complete(growthBalance: Double, onAutoAllocate: () -> Unit, onManualLater: () -> Unit) {

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(Modifier.height(32.dp))

        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(SuccessColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {

            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(44.dp), tint = SuccessColor)

        }

        Spacer(Modifier.height(20.dp))

        Text("设置完成！", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

        Text("您的财务配置已就绪", style = MaterialTheme.typography.bodyLarge, color = Gray600)

        Spacer(Modifier.height(24.dp))

        if (growthBalance > 0) {

            Card(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

                colors = CardDefaults.cardColors(containerColor = GrowthColor.copy(alpha = 0.08f))

            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Icon(Icons.Default.Info, contentDescription = null, tint = GrowthColor, modifier = Modifier.size(20.dp))

                        Spacer(Modifier.width(8.dp))

                        Text("增值投资账户有 ¥" + fmt2(growthBalance) + " 待分配", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = GrowthColor)

                    }

                    Spacer(Modifier.height(12.dp))

                    Text("请选择如何处理增值投资资金：", style = MaterialTheme.typography.bodyMedium, color = Gray700)

                    Spacer(Modifier.height(16.dp))

                    Button(onClick = onAutoAllocate, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = GrowthColor)) {

                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))

                        Spacer(Modifier.width(8.dp))

                        Text("自动按子账户比例分配")

                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(onClick = onManualLater, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp)) {

                        Text("稍后手动分配", color = Gray600)

                    }

                }

            }

        } else {

            Button(onClick = onAutoAllocate, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue600)) {

                Text("开始使用", style = MaterialTheme.typography.titleMedium)

            }

        }

        Spacer(Modifier.height(32.dp))

    }

}

// ─── Shared Components ─────────────────────────────────────────────────────

@Composable

private fun RatioSlider(label: String, emoji: String, value: Float, color: Color, onValueChange: (Float) -> Unit) {

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(emoji, style = MaterialTheme.typography.bodyLarge)

                Spacer(Modifier.width(4.dp))

                Text(label, style = MaterialTheme.typography.bodyMedium)

            }

            Text("${value.roundToLong()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)

        }

        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..100f, steps = 99, colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color, inactiveTrackColor = Gray200))

    }

}

@Composable

private fun AllocationRow(displayName: String, ratio: Float, amount: Double, color: Color) {

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))

            Spacer(Modifier.width(8.dp))

            Text("$displayName  ${ratio.roundToLong()}%", style = MaterialTheme.typography.bodyMedium)

        }

        Text("¥" + fmt2(amount), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

    }

}

