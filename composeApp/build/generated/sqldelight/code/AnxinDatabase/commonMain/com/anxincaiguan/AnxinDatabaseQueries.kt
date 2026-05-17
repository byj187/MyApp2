package com.anxincaiguan

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Double
import kotlin.Long
import kotlin.String

public class AnxinDatabaseQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAllMainAccounts(mapper: (
    id: Long,
    name: String,
    balance: Double,
    target_ratio_for_salary: Double,
  ) -> T): Query<T> = Query(1_867_632_002, arrayOf("main_account"), driver, "AnxinDatabase.sq",
      "selectAllMainAccounts",
      "SELECT main_account.id, main_account.name, main_account.balance, main_account.target_ratio_for_salary FROM main_account") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getDouble(2)!!,
      cursor.getDouble(3)!!
    )
  }

  public fun selectAllMainAccounts(): Query<Main_account> = selectAllMainAccounts { id, name,
      balance, target_ratio_for_salary ->
    Main_account(
      id,
      name,
      balance,
      target_ratio_for_salary
    )
  }

  public fun <T : Any> selectMainAccountById(id: Long, mapper: (
    id: Long,
    name: String,
    balance: Double,
    target_ratio_for_salary: Double,
  ) -> T): Query<T> = SelectMainAccountByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getDouble(2)!!,
      cursor.getDouble(3)!!
    )
  }

  public fun selectMainAccountById(id: Long): Query<Main_account> = selectMainAccountById(id) { id_,
      name, balance, target_ratio_for_salary ->
    Main_account(
      id_,
      name,
      balance,
      target_ratio_for_salary
    )
  }

  public fun <T : Any> selectAllGrowthSubAccounts(mapper: (
    id: Long,
    name: String,
    idle_amount: Double,
    invested_amount: Double,
    target_ratio: Double,
  ) -> T): Query<T> = Query(-2_024_545_508, arrayOf("growth_sub_account"), driver,
      "AnxinDatabase.sq", "selectAllGrowthSubAccounts",
      "SELECT growth_sub_account.id, growth_sub_account.name, growth_sub_account.idle_amount, growth_sub_account.invested_amount, growth_sub_account.target_ratio FROM growth_sub_account") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getDouble(2)!!,
      cursor.getDouble(3)!!,
      cursor.getDouble(4)!!
    )
  }

  public fun selectAllGrowthSubAccounts(): Query<Growth_sub_account> = selectAllGrowthSubAccounts {
      id, name, idle_amount, invested_amount, target_ratio ->
    Growth_sub_account(
      id,
      name,
      idle_amount,
      invested_amount,
      target_ratio
    )
  }

  public fun <T : Any> selectGrowthSubAccountById(id: Long, mapper: (
    id: Long,
    name: String,
    idle_amount: Double,
    invested_amount: Double,
    target_ratio: Double,
  ) -> T): Query<T> = SelectGrowthSubAccountByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getDouble(2)!!,
      cursor.getDouble(3)!!,
      cursor.getDouble(4)!!
    )
  }

  public fun selectGrowthSubAccountById(id: Long): Query<Growth_sub_account> =
      selectGrowthSubAccountById(id) { id_, name, idle_amount, invested_amount, target_ratio ->
    Growth_sub_account(
      id_,
      name,
      idle_amount,
      invested_amount,
      target_ratio
    )
  }

  public fun <T : Any> selectAllBills(mapper: (
    id: Long,
    account_type: String,
    account_id: Long,
    type: String,
    amount: Double,
    category: String,
    date: String,
    note: String,
  ) -> T): Query<T> = Query(1_482_224_105, arrayOf("bill"), driver, "AnxinDatabase.sq",
      "selectAllBills",
      "SELECT bill.id, bill.account_type, bill.account_id, bill.type, bill.amount, bill.category, bill.date, bill.note FROM bill ORDER BY date DESC") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getString(3)!!,
      cursor.getDouble(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!
    )
  }

  public fun selectAllBills(): Query<Bill> = selectAllBills { id, account_type, account_id, type,
      amount, category, date, note ->
    Bill(
      id,
      account_type,
      account_id,
      type,
      amount,
      category,
      date,
      note
    )
  }

  public fun <T : Any> selectBillsByAccount(
    account_type: String,
    account_id: Long,
    mapper: (
      id: Long,
      account_type: String,
      account_id: Long,
      type: String,
      amount: Double,
      category: String,
      date: String,
      note: String,
    ) -> T,
  ): Query<T> = SelectBillsByAccountQuery(account_type, account_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getString(3)!!,
      cursor.getDouble(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!
    )
  }

  public fun selectBillsByAccount(account_type: String, account_id: Long): Query<Bill> =
      selectBillsByAccount(account_type, account_id) { id, account_type_, account_id_, type, amount,
      category, date, note ->
    Bill(
      id,
      account_type_,
      account_id_,
      type,
      amount,
      category,
      date,
      note
    )
  }

  public fun <T : Any> selectAllTransfers(mapper: (
    id: Long,
    from_type: String,
    from_id: Long,
    to_type: String,
    to_id: Long,
    amount: Double,
    date: String,
    note: String,
  ) -> T): Query<T> = Query(-1_900_329_979, arrayOf("transfer"), driver, "AnxinDatabase.sq",
      "selectAllTransfers",
      "SELECT transfer.id, transfer.from_type, transfer.from_id, transfer.to_type, transfer.to_id, transfer.amount, transfer.date, transfer.note FROM transfer ORDER BY date DESC") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!
    )
  }

  public fun selectAllTransfers(): Query<Transfer> = selectAllTransfers { id, from_type, from_id,
      to_type, to_id, amount, date, note ->
    Transfer(
      id,
      from_type,
      from_id,
      to_type,
      to_id,
      amount,
      date,
      note
    )
  }

  public fun <T : Any> selectAllSalaryRecords(mapper: (
    id: Long,
    total: Double,
    date: String,
    daily_fixed: Double,
    remaining: Double,
    allocation_json: String,
  ) -> T): Query<T> = Query(571_006_133, arrayOf("salary_record"), driver, "AnxinDatabase.sq",
      "selectAllSalaryRecords",
      "SELECT salary_record.id, salary_record.total, salary_record.date, salary_record.daily_fixed, salary_record.remaining, salary_record.allocation_json FROM salary_record ORDER BY date DESC") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getDouble(1)!!,
      cursor.getString(2)!!,
      cursor.getDouble(3)!!,
      cursor.getDouble(4)!!,
      cursor.getString(5)!!
    )
  }

  public fun selectAllSalaryRecords(): Query<Salary_record> = selectAllSalaryRecords { id, total,
      date, daily_fixed, remaining, allocation_json ->
    Salary_record(
      id,
      total,
      date,
      daily_fixed,
      remaining,
      allocation_json
    )
  }

  public fun <T : Any> selectProductsBySubAccount(sub_account_id: Long, mapper: (
    id: Long,
    sub_account_id: Long,
    product_name: String,
    amount: Double,
    rate: Double,
    purchase_date: String,
    expire_date: String?,
    note: String,
    status: String,
  ) -> T): Query<T> = SelectProductsBySubAccountQuery(sub_account_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getDouble(3)!!,
      cursor.getDouble(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6),
      cursor.getString(7)!!,
      cursor.getString(8)!!
    )
  }

  public fun selectProductsBySubAccount(sub_account_id: Long): Query<Investment_product> =
      selectProductsBySubAccount(sub_account_id) { id, sub_account_id_, product_name, amount, rate,
      purchase_date, expire_date, note, status ->
    Investment_product(
      id,
      sub_account_id_,
      product_name,
      amount,
      rate,
      purchase_date,
      expire_date,
      note,
      status
    )
  }

  public fun <T : Any> selectAllInvestmentIncomes(mapper: (
    id: Long,
    product_id: Long,
    amount: Double,
    income_type: String,
    date: String,
    note: String,
  ) -> T): Query<T> = Query(-704_362_828, arrayOf("investment_income"), driver, "AnxinDatabase.sq",
      "selectAllInvestmentIncomes",
      "SELECT investment_income.id, investment_income.product_id, investment_income.amount, investment_income.income_type, investment_income.date, investment_income.note FROM investment_income ORDER BY date DESC") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getDouble(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!
    )
  }

  public fun selectAllInvestmentIncomes(): Query<Investment_income> = selectAllInvestmentIncomes {
      id, product_id, amount, income_type, date, note ->
    Investment_income(
      id,
      product_id,
      amount,
      income_type,
      date,
      note
    )
  }

  public fun <T : Any> selectIncomesByProduct(product_id: Long, mapper: (
    id: Long,
    product_id: Long,
    amount: Double,
    income_type: String,
    date: String,
    note: String,
  ) -> T): Query<T> = SelectIncomesByProductQuery(product_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getDouble(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!
    )
  }

  public fun selectIncomesByProduct(product_id: Long): Query<Investment_income> =
      selectIncomesByProduct(product_id) { id, product_id_, amount, income_type, date, note ->
    Investment_income(
      id,
      product_id_,
      amount,
      income_type,
      date,
      note
    )
  }

  public fun <T : Any> selectStableProductsByAccount(account_id: Long, mapper: (
    id: Long,
    account_id: Long,
    product_name: String,
    product_type: String,
    amount: Double,
    annual_rate: Double,
    purchase_date: String,
    expire_date: String?,
    note: String,
    status: String,
  ) -> T): Query<T> = SelectStableProductsByAccountQuery(account_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getDouble(4)!!,
      cursor.getDouble(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7),
      cursor.getString(8)!!,
      cursor.getString(9)!!
    )
  }

  public fun selectStableProductsByAccount(account_id: Long): Query<Stable_product> =
      selectStableProductsByAccount(account_id) { id, account_id_, product_name, product_type,
      amount, annual_rate, purchase_date, expire_date, note, status ->
    Stable_product(
      id,
      account_id_,
      product_name,
      product_type,
      amount,
      annual_rate,
      purchase_date,
      expire_date,
      note,
      status
    )
  }

  public fun <T : Any> selectStableProductById(id: Long, mapper: (
    id: Long,
    account_id: Long,
    product_name: String,
    product_type: String,
    amount: Double,
    annual_rate: Double,
    purchase_date: String,
    expire_date: String?,
    note: String,
    status: String,
  ) -> T): Query<T> = SelectStableProductByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getDouble(4)!!,
      cursor.getDouble(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7),
      cursor.getString(8)!!,
      cursor.getString(9)!!
    )
  }

  public fun selectStableProductById(id: Long): Query<Stable_product> =
      selectStableProductById(id) { id_, account_id, product_name, product_type, amount,
      annual_rate, purchase_date, expire_date, note, status ->
    Stable_product(
      id_,
      account_id,
      product_name,
      product_type,
      amount,
      annual_rate,
      purchase_date,
      expire_date,
      note,
      status
    )
  }

  public fun <T : Any> selectAllStableIncomes(mapper: (
    id: Long,
    product_id: Long,
    amount: Double,
    date: String,
    note: String,
  ) -> T): Query<T> = Query(-557_891_636, arrayOf("stable_income"), driver, "AnxinDatabase.sq",
      "selectAllStableIncomes",
      "SELECT stable_income.id, stable_income.product_id, stable_income.amount, stable_income.date, stable_income.note FROM stable_income ORDER BY date DESC") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getDouble(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!
    )
  }

  public fun selectAllStableIncomes(): Query<Stable_income> = selectAllStableIncomes { id,
      product_id, amount, date, note ->
    Stable_income(
      id,
      product_id,
      amount,
      date,
      note
    )
  }

  public fun <T : Any> selectStableIncomesByProduct(product_id: Long, mapper: (
    id: Long,
    product_id: Long,
    amount: Double,
    date: String,
    note: String,
  ) -> T): Query<T> = SelectStableIncomesByProductQuery(product_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getDouble(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!
    )
  }

  public fun selectStableIncomesByProduct(product_id: Long): Query<Stable_income> =
      selectStableIncomesByProduct(product_id) { id, product_id_, amount, date, note ->
    Stable_income(
      id,
      product_id_,
      amount,
      date,
      note
    )
  }

  public fun <T : Any> selectSettings(mapper: (
    id: Long,
    main_ratios_for_salary: String,
    growth_ratios: String,
    deviation_threshold: Double,
    daily_fixed_amount: Double,
    stock_asset_ratios: String,
  ) -> T): Query<T> = Query(1_145_691_617, arrayOf("settings"), driver, "AnxinDatabase.sq",
      "selectSettings",
      "SELECT settings.id, settings.main_ratios_for_salary, settings.growth_ratios, settings.deviation_threshold, settings.daily_fixed_amount, settings.stock_asset_ratios FROM settings WHERE id = 1") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getDouble(3)!!,
      cursor.getDouble(4)!!,
      cursor.getString(5)!!
    )
  }

  public fun selectSettings(): Query<Settings> = selectSettings { id, main_ratios_for_salary,
      growth_ratios, deviation_threshold, daily_fixed_amount, stock_asset_ratios ->
    Settings(
      id,
      main_ratios_for_salary,
      growth_ratios,
      deviation_threshold,
      daily_fixed_amount,
      stock_asset_ratios
    )
  }

  public fun insertMainAccount(
    name: String,
    balance: Double,
    target_ratio_for_salary: Double,
  ) {
    driver.execute(-1_838_288_423,
        """INSERT INTO main_account (name, balance, target_ratio_for_salary) VALUES (?, ?, ?)""", 3)
        {
          bindString(0, name)
          bindDouble(1, balance)
          bindDouble(2, target_ratio_for_salary)
        }
    notifyQueries(-1_838_288_423) { emit ->
      emit("main_account")
    }
  }

  public fun updateMainAccountBalance(balance: Double, id: Long) {
    driver.execute(-1_282_076_461, """UPDATE main_account SET balance = balance + ? WHERE id = ?""",
        2) {
          bindDouble(0, balance)
          bindLong(1, id)
        }
    notifyQueries(-1_282_076_461) { emit ->
      emit("main_account")
    }
  }

  public fun updateMainAccountRatio(target_ratio_for_salary: Double, id: Long) {
    driver.execute(236_913_602,
        """UPDATE main_account SET target_ratio_for_salary = ? WHERE id = ?""", 2) {
          bindDouble(0, target_ratio_for_salary)
          bindLong(1, id)
        }
    notifyQueries(236_913_602) { emit ->
      emit("main_account")
    }
  }

  public fun deleteAllMainAccounts() {
    driver.execute(-703_125_261, """DELETE FROM main_account""", 0)
    notifyQueries(-703_125_261) { emit ->
      emit("main_account")
    }
  }

  public fun insertGrowthSubAccount(
    name: String,
    idle_amount: Double,
    invested_amount: Double,
    target_ratio: Double,
  ) {
    driver.execute(-2_076_818_321,
        """INSERT INTO growth_sub_account (name, idle_amount, invested_amount, target_ratio) VALUES (?, ?, ?, ?)""",
        4) {
          bindString(0, name)
          bindDouble(1, idle_amount)
          bindDouble(2, invested_amount)
          bindDouble(3, target_ratio)
        }
    notifyQueries(-2_076_818_321) { emit ->
      emit("growth_sub_account")
    }
  }

  public fun updateGrowthSubAccountIdle(idle_amount: Double, id: Long) {
    driver.execute(-1_082_460_429,
        """UPDATE growth_sub_account SET idle_amount = idle_amount + ? WHERE id = ?""", 2) {
          bindDouble(0, idle_amount)
          bindLong(1, id)
        }
    notifyQueries(-1_082_460_429) { emit ->
      emit("growth_sub_account")
    }
  }

  public fun updateGrowthSubAccountInvested(invested_amount: Double, id: Long) {
    driver.execute(750_064_371,
        """UPDATE growth_sub_account SET invested_amount = invested_amount + ? WHERE id = ?""", 2) {
          bindDouble(0, invested_amount)
          bindLong(1, id)
        }
    notifyQueries(750_064_371) { emit ->
      emit("growth_sub_account")
    }
  }

  public fun deleteAllGrowthSubAccounts() {
    driver.execute(-1_652_357_557, """DELETE FROM growth_sub_account""", 0)
    notifyQueries(-1_652_357_557) { emit ->
      emit("growth_sub_account")
    }
  }

  public fun insertBill(
    account_type: String,
    account_id: Long,
    type: String,
    amount: Double,
    category: String,
    date: String,
    note: String,
  ) {
    driver.execute(1_294_089_218,
        """INSERT INTO bill (account_type, account_id, type, amount, category, date, note) VALUES (?, ?, ?, ?, ?, ?, ?)""",
        7) {
          bindString(0, account_type)
          bindLong(1, account_id)
          bindString(2, type)
          bindDouble(3, amount)
          bindString(4, category)
          bindString(5, date)
          bindString(6, note)
        }
    notifyQueries(1_294_089_218) { emit ->
      emit("bill")
    }
  }

  public fun deleteBill(id: Long) {
    driver.execute(2_092_512_756, """DELETE FROM bill WHERE id = ?""", 1) {
          bindLong(0, id)
        }
    notifyQueries(2_092_512_756) { emit ->
      emit("bill")
    }
  }

  public fun deleteAllBills() {
    driver.execute(-1_523_047_528, """DELETE FROM bill""", 0)
    notifyQueries(-1_523_047_528) { emit ->
      emit("bill")
    }
  }

  public fun insertTransfer(
    from_type: String,
    from_id: Long,
    to_type: String,
    to_id: Long,
    amount: Double,
    date: String,
    note: String,
  ) {
    driver.execute(1_362_778_726,
        """INSERT INTO transfer (from_type, from_id, to_type, to_id, amount, date, note) VALUES (?, ?, ?, ?, ?, ?, ?)""",
        7) {
          bindString(0, from_type)
          bindLong(1, from_id)
          bindString(2, to_type)
          bindLong(3, to_id)
          bindDouble(4, amount)
          bindString(5, date)
          bindString(6, note)
        }
    notifyQueries(1_362_778_726) { emit ->
      emit("transfer")
    }
  }

  public fun deleteAllTransfers() {
    driver.execute(272_369_204, """DELETE FROM transfer""", 0)
    notifyQueries(272_369_204) { emit ->
      emit("transfer")
    }
  }

  public fun insertSalaryRecord(
    total: Double,
    date: String,
    daily_fixed: Double,
    remaining: Double,
    allocation_json: String,
  ) {
    driver.execute(-1_893_199_946,
        """INSERT INTO salary_record (total, date, daily_fixed, remaining, allocation_json) VALUES (?, ?, ?, ?, ?)""",
        5) {
          bindDouble(0, total)
          bindString(1, date)
          bindDouble(2, daily_fixed)
          bindDouble(3, remaining)
          bindString(4, allocation_json)
        }
    notifyQueries(-1_893_199_946) { emit ->
      emit("salary_record")
    }
  }

  public fun deleteAllSalaryRecords() {
    driver.execute(-1_813_057_692, """DELETE FROM salary_record""", 0)
    notifyQueries(-1_813_057_692) { emit ->
      emit("salary_record")
    }
  }

  public fun insertInvestmentProduct(
    sub_account_id: Long,
    product_name: String,
    amount: Double,
    rate: Double,
    purchase_date: String,
    expire_date: String?,
    note: String,
    status: String,
  ) {
    driver.execute(-888_805_759,
        """INSERT INTO investment_product (sub_account_id, product_name, amount, rate, purchase_date, expire_date, note, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
        8) {
          bindLong(0, sub_account_id)
          bindString(1, product_name)
          bindDouble(2, amount)
          bindDouble(3, rate)
          bindString(4, purchase_date)
          bindString(5, expire_date)
          bindString(6, note)
          bindString(7, status)
        }
    notifyQueries(-888_805_759) { emit ->
      emit("investment_product")
    }
  }

  public fun updateProductAmount(amount: Double, id: Long) {
    driver.execute(-1_503_006_596, """UPDATE investment_product SET amount = ? WHERE id = ?""", 2) {
          bindDouble(0, amount)
          bindLong(1, id)
        }
    notifyQueries(-1_503_006_596) { emit ->
      emit("investment_product")
    }
  }

  public fun updateProductStatus(status: String, id: Long) {
    driver.execute(-981_635_050, """UPDATE investment_product SET status = ? WHERE id = ?""", 2) {
          bindString(0, status)
          bindLong(1, id)
        }
    notifyQueries(-981_635_050) { emit ->
      emit("investment_product")
    }
  }

  public fun deleteAllInvestmentProducts() {
    driver.execute(1_490_926_219, """DELETE FROM investment_product""", 0)
    notifyQueries(1_490_926_219) { emit ->
      emit("investment_product")
    }
  }

  public fun insertInvestmentIncome(
    product_id: Long,
    amount: Double,
    income_type: String,
    date: String,
    note: String,
  ) {
    driver.execute(43_978_199,
        """INSERT INTO investment_income (product_id, amount, income_type, date, note) VALUES (?, ?, ?, ?, ?)""",
        5) {
          bindLong(0, product_id)
          bindDouble(1, amount)
          bindString(2, income_type)
          bindString(3, date)
          bindString(4, note)
        }
    notifyQueries(43_978_199) { emit ->
      emit("investment_income")
    }
  }

  public fun deleteAllInvestmentIncomes() {
    driver.execute(-332_174_877, """DELETE FROM investment_income""", 0)
    notifyQueries(-332_174_877) { emit ->
      emit("investment_income")
    }
  }

  public fun insertStableProduct(
    account_id: Long,
    product_name: String,
    product_type: String,
    amount: Double,
    annual_rate: Double,
    purchase_date: String,
    expire_date: String?,
    note: String,
    status: String,
  ) {
    driver.execute(-1_940_683_879,
        """INSERT INTO stable_product (account_id, product_name, product_type, amount, annual_rate, purchase_date, expire_date, note, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        9) {
          bindLong(0, account_id)
          bindString(1, product_name)
          bindString(2, product_type)
          bindDouble(3, amount)
          bindDouble(4, annual_rate)
          bindString(5, purchase_date)
          bindString(6, expire_date)
          bindString(7, note)
          bindString(8, status)
        }
    notifyQueries(-1_940_683_879) { emit ->
      emit("stable_product")
    }
  }

  public fun updateStableProductAmount(amount: Double, id: Long) {
    driver.execute(-484_697_791, """UPDATE stable_product SET amount = ? WHERE id = ?""", 2) {
          bindDouble(0, amount)
          bindLong(1, id)
        }
    notifyQueries(-484_697_791) { emit ->
      emit("stable_product")
    }
  }

  public fun updateStableProductStatus(status: String, id: Long) {
    driver.execute(36_673_755, """UPDATE stable_product SET status = ? WHERE id = ?""", 2) {
          bindString(0, status)
          bindLong(1, id)
        }
    notifyQueries(36_673_755) { emit ->
      emit("stable_product")
    }
  }

  public fun deleteAllStableProducts() {
    driver.execute(-2_102_860_557, """DELETE FROM stable_product""", 0)
    notifyQueries(-2_102_860_557) { emit ->
      emit("stable_product")
    }
  }

  public fun insertStableIncome(
    product_id: Long,
    amount: Double,
    date: String,
    note: String,
  ) {
    driver.execute(287_141_311,
        """INSERT INTO stable_income (product_id, amount, date, note) VALUES (?, ?, ?, ?)""", 4) {
          bindLong(0, product_id)
          bindDouble(1, amount)
          bindString(2, date)
          bindString(3, note)
        }
    notifyQueries(287_141_311) { emit ->
      emit("stable_income")
    }
  }

  public fun deleteAllStableIncomes() {
    driver.execute(1_353_011_835, """DELETE FROM stable_income""", 0)
    notifyQueries(1_353_011_835) { emit ->
      emit("stable_income")
    }
  }

  public fun insertOrReplaceSettings(
    main_ratios_for_salary: String,
    growth_ratios: String,
    deviation_threshold: Double,
    daily_fixed_amount: Double,
    stock_asset_ratios: String,
  ) {
    driver.execute(1_714_593_401,
        """INSERT OR REPLACE INTO settings (id, main_ratios_for_salary, growth_ratios, deviation_threshold, daily_fixed_amount, stock_asset_ratios) VALUES (1, ?, ?, ?, ?, ?)""",
        5) {
          bindString(0, main_ratios_for_salary)
          bindString(1, growth_ratios)
          bindDouble(2, deviation_threshold)
          bindDouble(3, daily_fixed_amount)
          bindString(4, stock_asset_ratios)
        }
    notifyQueries(1_714_593_401) { emit ->
      emit("settings")
    }
  }

  private inner class SelectMainAccountByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("main_account", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("main_account", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-473_199_448,
        """SELECT main_account.id, main_account.name, main_account.balance, main_account.target_ratio_for_salary FROM main_account WHERE id = ?""",
        mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "AnxinDatabase.sq:selectMainAccountById"
  }

  private inner class SelectGrowthSubAccountByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("growth_sub_account", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("growth_sub_account", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-459_671_644,
        """SELECT growth_sub_account.id, growth_sub_account.name, growth_sub_account.idle_amount, growth_sub_account.invested_amount, growth_sub_account.target_ratio FROM growth_sub_account WHERE id = ?""",
        mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "AnxinDatabase.sq:selectGrowthSubAccountById"
  }

  private inner class SelectBillsByAccountQuery<out T : Any>(
    public val account_type: String,
    public val account_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("bill", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("bill", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-180_803_288,
        """SELECT bill.id, bill.account_type, bill.account_id, bill.type, bill.amount, bill.category, bill.date, bill.note FROM bill WHERE account_type = ? AND account_id = ? ORDER BY date DESC""",
        mapper, 2) {
      bindString(0, account_type)
      bindLong(1, account_id)
    }

    override fun toString(): String = "AnxinDatabase.sq:selectBillsByAccount"
  }

  private inner class SelectProductsBySubAccountQuery<out T : Any>(
    public val sub_account_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("investment_product", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("investment_product", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(415_246_278,
        """SELECT investment_product.id, investment_product.sub_account_id, investment_product.product_name, investment_product.amount, investment_product.rate, investment_product.purchase_date, investment_product.expire_date, investment_product.note, investment_product.status FROM investment_product WHERE sub_account_id = ?""",
        mapper, 1) {
      bindLong(0, sub_account_id)
    }

    override fun toString(): String = "AnxinDatabase.sq:selectProductsBySubAccount"
  }

  private inner class SelectIncomesByProductQuery<out T : Any>(
    public val product_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("investment_income", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("investment_income", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_598_418_828,
        """SELECT investment_income.id, investment_income.product_id, investment_income.amount, investment_income.income_type, investment_income.date, investment_income.note FROM investment_income WHERE product_id = ? ORDER BY date DESC""",
        mapper, 1) {
      bindLong(0, product_id)
    }

    override fun toString(): String = "AnxinDatabase.sq:selectIncomesByProduct"
  }

  private inner class SelectStableProductsByAccountQuery<out T : Any>(
    public val account_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("stable_product", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("stable_product", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-2_020_535_975,
        """SELECT stable_product.id, stable_product.account_id, stable_product.product_name, stable_product.product_type, stable_product.amount, stable_product.annual_rate, stable_product.purchase_date, stable_product.expire_date, stable_product.note, stable_product.status FROM stable_product WHERE account_id = ?""",
        mapper, 1) {
      bindLong(0, account_id)
    }

    override fun toString(): String = "AnxinDatabase.sq:selectStableProductsByAccount"
  }

  private inner class SelectStableProductByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("stable_product", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("stable_product", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(796_384_040,
        """SELECT stable_product.id, stable_product.account_id, stable_product.product_name, stable_product.product_type, stable_product.amount, stable_product.annual_rate, stable_product.purchase_date, stable_product.expire_date, stable_product.note, stable_product.status FROM stable_product WHERE id = ?""",
        mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "AnxinDatabase.sq:selectStableProductById"
  }

  private inner class SelectStableIncomesByProductQuery<out T : Any>(
    public val product_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("stable_income", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("stable_income", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(2_075_763_303,
        """SELECT stable_income.id, stable_income.product_id, stable_income.amount, stable_income.date, stable_income.note FROM stable_income WHERE product_id = ? ORDER BY date DESC""",
        mapper, 1) {
      bindLong(0, product_id)
    }

    override fun toString(): String = "AnxinDatabase.sq:selectStableIncomesByProduct"
  }
}
