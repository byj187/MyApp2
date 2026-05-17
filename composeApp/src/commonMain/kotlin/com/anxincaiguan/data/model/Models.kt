package com.anxincaiguan.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MainAccount(
    val id: Long = 0,
    val name: String,
    val balance: Double = 0.0,
    val targetRatioForSalary: Double = 0.0
)

@Serializable
data class GrowthSubAccount(
    val id: Long = 0,
    val name: String,
    val idleAmount: Double = 0.0,
    val investedAmount: Double = 0.0,
    val targetRatio: Double = 0.0
)

@Serializable
data class Bill(
    val id: Long = 0,
    val accountType: String,
    val accountId: Long,
    val type: String,
    val amount: Double,
    val category: String,
    val date: String,
    val note: String = ""
)

@Serializable
data class Transfer(
    val id: Long = 0,
    val fromType: String,
    val fromId: Long,
    val toType: String,
    val toId: Long,
    val amount: Double,
    val date: String,
    val note: String = ""
)

@Serializable
data class SalaryRecord(
    val id: Long = 0,
    val total: Double,
    val date: String,
    val dailyFixed: Double,
    val remaining: Double,
    val allocationJson: String = ""
)

@Serializable
data class InvestmentProduct(
    val id: Long = 0,
    val subAccountId: Long,
    val productName: String,
    val amount: Double,
    val rate: Double = 0.0,
    val purchaseDate: String,
    val expireDate: String? = null,
    val note: String = "",
    val status: String = "ACTIVE"
)

@Serializable
data class InvestmentIncome(
    val id: Long = 0,
    val productId: Long,
    val amount: Double,
    val incomeType: String,
    val date: String,
    val note: String = ""
)

@Serializable
data class StableProduct(
    val id: Long = 0,
    val accountId: Long,
    val productName: String,
    val productType: String,
    val amount: Double,
    val annualRate: Double = 0.0,
    val purchaseDate: String,
    val expireDate: String? = null,
    val note: String = "",
    val status: String = "ACTIVE"
)

@Serializable
data class StableIncome(
    val id: Long = 0,
    val productId: Long,
    val amount: Double,
    val date: String,
    val note: String = ""
)

enum class StableProductType(val displayName: String, val emoji: String) {
    TIME_DEPOSIT("定期存款", "🏦"),
    GOVERNMENT_BOND("国债/地方债", "📜"),
    MONEY_MARKET("货币基金", "💰"),
    STRUCTURED_DEPOSIT("结构性存款", "🏛️");

    companion object {
        fun fromName(name: String): StableProductType =
            entries.firstOrNull { it.displayName == name } ?: TIME_DEPOSIT
    }
}

@Serializable
data class AppSettings(
    val id: Long = 1,
    val mainRatiosForSalary: String = "{}",
    val growthRatios: String = "{}",
    val deviationThreshold: Double = 0.05,
    val dailyFixedAmount: Double = 0.0,
    val stockAssetRatios: String = "{}"
)

enum class MainAccountType(val displayName: String, val emoji: String) {
    DAILY("日常消费", "💳"),
    QUALITY("生活品质", "🎭"),
    STABLE("稳健保底", "🛡️"),
    GROWTH("增值投资", "📈");

    companion object {
        fun fromName(name: String): MainAccountType =
            entries.firstOrNull { it.displayName == name } ?: DAILY
    }
}

enum class GrowthSubAccountType(val displayName: String, val emoji: String) {
    INDEX_FUND("指数基金", "📊"),
    ACTIVE_EQUITY("主动权益基金", "🎯"),
    BOND_FIXED_INCOME("债券/固收", "🔒"),
    OTHER_ALTERNATIVE("其他另类", "💎");

    companion object {
        fun fromName(name: String): GrowthSubAccountType =
            entries.firstOrNull { it.displayName == name } ?: INDEX_FUND
    }
}
