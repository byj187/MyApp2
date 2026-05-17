package com.anxincaiguan.data.local

import com.anxincaiguan.AnxinDatabase
import com.anxincaiguan.data.model.*
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val driver: SqlDriver) {
    private val database = AnxinDatabase(driver)
    private val queries = database.anxinDatabaseQueries

    suspend fun isOnboarded(): Boolean = withContext(Dispatchers.Default) {
        val settings = queries.selectSettings().executeAsOneOrNull()
        settings != null
    }

    suspend fun saveMainAccounts(accounts: List<MainAccount>) = withContext(Dispatchers.Default) {
        queries.deleteAllMainAccounts()
        accounts.forEach { account ->
            queries.insertMainAccount(
                name = account.name,
                balance = account.balance,
                target_ratio_for_salary = account.targetRatioForSalary
            )
        }
    }

    suspend fun saveGrowthSubAccounts(accounts: List<GrowthSubAccount>) = withContext(Dispatchers.Default) {
        queries.deleteAllGrowthSubAccounts()
        accounts.forEach { account ->
            queries.insertGrowthSubAccount(
                name = account.name,
                idle_amount = account.idleAmount,
                invested_amount = account.investedAmount,
                target_ratio = account.targetRatio
            )
        }
    }

    suspend fun saveSettings(settings: AppSettings) = withContext(Dispatchers.Default) {
        queries.insertOrReplaceSettings(
            main_ratios_for_salary = settings.mainRatiosForSalary,
            growth_ratios = settings.growthRatios,
            deviation_threshold = settings.deviationThreshold,
            daily_fixed_amount = settings.dailyFixedAmount,
            stock_asset_ratios = settings.stockAssetRatios
        )
    }

    suspend fun getAllMainAccounts(): List<MainAccount> = withContext(Dispatchers.Default) {
        queries.selectAllMainAccounts().executeAsList().map { row ->
            MainAccount(
                id = row.id,
                name = row.name,
                balance = row.balance,
                targetRatioForSalary = row.target_ratio_for_salary
            )
        }
    }

    suspend fun getAllGrowthSubAccounts(): List<GrowthSubAccount> = withContext(Dispatchers.Default) {
        queries.selectAllGrowthSubAccounts().executeAsList().map { row ->
            GrowthSubAccount(
                id = row.id,
                name = row.name,
                idleAmount = row.idle_amount,
                investedAmount = row.invested_amount,
                targetRatio = row.target_ratio
            )
        }
    }

    suspend fun getSettings(): AppSettings? = withContext(Dispatchers.Default) {
        queries.selectSettings().executeAsOneOrNull()?.let { row ->
            AppSettings(
                id = row.id,
                mainRatiosForSalary = row.main_ratios_for_salary,
                growthRatios = row.growth_ratios,
                deviationThreshold = row.deviation_threshold,
                dailyFixedAmount = row.daily_fixed_amount,
                stockAssetRatios = row.stock_asset_ratios
            )
        }
    }

    suspend fun insertBill(bill: Bill) = withContext(Dispatchers.Default) {
        queries.insertBill(
            account_type = bill.accountType,
            account_id = bill.accountId,
            type = bill.type,
            amount = bill.amount,
            category = bill.category,
            date = bill.date,
            note = bill.note
        )
    }

    suspend fun getAllBills(): List<Bill> = withContext(Dispatchers.Default) {
        queries.selectAllBills().executeAsList().map { row ->
            Bill(
                id = row.id,
                accountType = row.account_type,
                accountId = row.account_id,
                type = row.type,
                amount = row.amount,
                category = row.category,
                date = row.date,
                note = row.note
            )
        }
    }

    suspend fun getBillsByAccount(accountType: String, accountId: Long): List<Bill> = withContext(Dispatchers.Default) {
        queries.selectBillsByAccount(accountType, accountId).executeAsList().map { row ->
            Bill(
                id = row.id,
                accountType = row.account_type,
                accountId = row.account_id,
                type = row.type,
                amount = row.amount,
                category = row.category,
                date = row.date,
                note = row.note
            )
        }
    }

    suspend fun deleteBill(billId: Long) = withContext(Dispatchers.Default) {
        queries.deleteBill(billId)
    }

    suspend fun getAllTransfers(): List<Transfer> = withContext(Dispatchers.Default) {
        queries.selectAllTransfers().executeAsList().map { row ->
            Transfer(
                id = row.id,
                fromType = row.from_type,
                fromId = row.from_id,
                toType = row.to_type,
                toId = row.to_id,
                amount = row.amount,
                date = row.date,
                note = row.note
            )
        }
    }

    suspend fun insertTransfer(transfer: Transfer) = withContext(Dispatchers.Default) {
        queries.insertTransfer(
            from_type = transfer.fromType,
            from_id = transfer.fromId,
            to_type = transfer.toType,
            to_id = transfer.toId,
            amount = transfer.amount,
            date = transfer.date,
            note = transfer.note
        )
    }

    suspend fun insertSalaryRecord(record: SalaryRecord) = withContext(Dispatchers.Default) {
        queries.insertSalaryRecord(
            total = record.total,
            date = record.date,
            daily_fixed = record.dailyFixed,
            remaining = record.remaining,
            allocation_json = record.allocationJson
        )
    }

    suspend fun getAllSalaryRecords(): List<SalaryRecord> = withContext(Dispatchers.Default) {
        queries.selectAllSalaryRecords().executeAsList().map { row ->
            SalaryRecord(
                id = row.id,
                total = row.total,
                date = row.date,
                dailyFixed = row.daily_fixed,
                remaining = row.remaining,
                allocationJson = row.allocation_json
            )
        }
    }

    suspend fun getAllInvestmentIncomes(): List<InvestmentIncome> = withContext(Dispatchers.Default) {
        queries.selectAllInvestmentIncomes().executeAsList().map { row ->
            InvestmentIncome(
                id = row.id,
                productId = row.product_id,
                amount = row.amount,
                incomeType = row.income_type,
                date = row.date,
                note = row.note
            )
        }
    }

    suspend fun insertInvestmentIncome(income: InvestmentIncome) = withContext(Dispatchers.Default) {
        queries.insertInvestmentIncome(
            product_id = income.productId,
            amount = income.amount,
            income_type = income.incomeType,
            date = income.date,
            note = income.note
        )
    }

    suspend fun getMainAccountById(id: Long): MainAccount? = withContext(Dispatchers.Default) {
        queries.selectMainAccountById(id).executeAsOneOrNull()?.let {
            MainAccount(id = it.id, name = it.name, balance = it.balance, targetRatioForSalary = it.target_ratio_for_salary)
        }
    }

    suspend fun getGrowthSubAccountById(id: Long): GrowthSubAccount? = withContext(Dispatchers.Default) {
        queries.selectGrowthSubAccountById(id).executeAsOneOrNull()?.let {
            GrowthSubAccount(id = it.id, name = it.name, idleAmount = it.idle_amount, investedAmount = it.invested_amount, targetRatio = it.target_ratio)
        }
    }

    suspend fun updateMainAccountBalance(accountId: Long, delta: Double) = withContext(Dispatchers.Default) {
        queries.updateMainAccountBalance(delta, accountId)
    }

    suspend fun updateGrowthSubAccountIdle(subAccountId: Long, delta: Double) = withContext(Dispatchers.Default) {
        queries.updateGrowthSubAccountIdle(delta, subAccountId)
    }

    suspend fun updateGrowthSubAccountInvested(subAccountId: Long, delta: Double) = withContext(Dispatchers.Default) {
        queries.updateGrowthSubAccountInvested(delta, subAccountId)
    }

    suspend fun getProductsBySubAccount(subAccountId: Long): List<InvestmentProduct> = withContext(Dispatchers.Default) {
        queries.selectProductsBySubAccount(subAccountId).executeAsList().map { row ->
            InvestmentProduct(
                id = row.id,
                subAccountId = row.sub_account_id,
                productName = row.product_name,
                amount = row.amount,
                rate = row.rate,
                purchaseDate = row.purchase_date,
                expireDate = row.expire_date,
                note = row.note,
                status = row.status
            )
        }
    }

    suspend fun insertInvestmentProduct(product: InvestmentProduct) = withContext(Dispatchers.Default) {
        queries.insertInvestmentProduct(
            sub_account_id = product.subAccountId,
            product_name = product.productName,
            amount = product.amount,
            rate = product.rate,
            purchase_date = product.purchaseDate,
            expire_date = product.expireDate,
            note = product.note,
            status = product.status
        )
    }

    suspend fun updateProductAmount(productId: Long, amount: Double) = withContext(Dispatchers.Default) {
        queries.updateProductAmount(amount, productId)
    }

    suspend fun updateProductStatus(productId: Long, status: String) = withContext(Dispatchers.Default) {
        queries.updateProductStatus(status, productId)
    }

    suspend fun clearAllData() = withContext(Dispatchers.Default) {
        queries.deleteAllMainAccounts()
        queries.deleteAllGrowthSubAccounts()
        queries.deleteAllBills()
        queries.deleteAllTransfers()
        queries.deleteAllSalaryRecords()
        queries.deleteAllInvestmentProducts()
        queries.deleteAllInvestmentIncomes()
        queries.deleteAllStableProducts()
        queries.deleteAllStableIncomes()
    }

    suspend fun getStableProductsByAccount(accountId: Long): List<StableProduct> = withContext(Dispatchers.Default) {
        queries.selectStableProductsByAccount(accountId).executeAsList().map { row ->
            StableProduct(
                id = row.id,
                accountId = row.account_id,
                productName = row.product_name,
                productType = row.product_type,
                amount = row.amount,
                annualRate = row.annual_rate,
                purchaseDate = row.purchase_date,
                expireDate = row.expire_date,
                note = row.note,
                status = row.status
            )
        }
    }

    suspend fun insertStableProduct(product: StableProduct) = withContext(Dispatchers.Default) {
        queries.insertStableProduct(
            account_id = product.accountId,
            product_name = product.productName,
            product_type = product.productType,
            amount = product.amount,
            annual_rate = product.annualRate,
            purchase_date = product.purchaseDate,
            expire_date = product.expireDate,
            note = product.note,
            status = product.status
        )
    }

    suspend fun updateStableProductAmount(productId: Long, amount: Double) = withContext(Dispatchers.Default) {
        queries.updateStableProductAmount(amount, productId)
    }

    suspend fun updateStableProductStatus(productId: Long, status: String) = withContext(Dispatchers.Default) {
        queries.updateStableProductStatus(status, productId)
    }

    suspend fun getStableIncomesByProduct(productId: Long): List<StableIncome> = withContext(Dispatchers.Default) {
        queries.selectStableIncomesByProduct(productId).executeAsList().map { row ->
            StableIncome(
                id = row.id,
                productId = row.product_id,
                amount = row.amount,
                date = row.date,
                note = row.note
            )
        }
    }

    suspend fun getAllStableIncomes(): List<StableIncome> = withContext(Dispatchers.Default) {
        queries.selectAllStableIncomes().executeAsList().map { row ->
            StableIncome(
                id = row.id,
                productId = row.product_id,
                amount = row.amount,
                date = row.date,
                note = row.note
            )
        }
    }

    suspend fun insertStableIncome(income: StableIncome) = withContext(Dispatchers.Default) {
        queries.insertStableIncome(
            product_id = income.productId,
            amount = income.amount,
            date = income.date,
            note = income.note
        )
    }
}
