package com.anxincaiguan.composeApp

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.anxincaiguan.AnxinDatabase
import com.anxincaiguan.AnxinDatabaseQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<AnxinDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = AnxinDatabaseImpl.Schema

internal fun KClass<AnxinDatabase>.newInstance(driver: SqlDriver): AnxinDatabase =
    AnxinDatabaseImpl(driver)

private class AnxinDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), AnxinDatabase {
  override val anxinDatabaseQueries: AnxinDatabaseQueries = AnxinDatabaseQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE main_account (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    name TEXT NOT NULL,
          |    balance REAL NOT NULL DEFAULT 0.0,
          |    target_ratio_for_salary REAL NOT NULL DEFAULT 0.0
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE growth_sub_account (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    name TEXT NOT NULL,
          |    idle_amount REAL NOT NULL DEFAULT 0.0,
          |    invested_amount REAL NOT NULL DEFAULT 0.0,
          |    target_ratio REAL NOT NULL DEFAULT 0.0
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE bill (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    account_type TEXT NOT NULL,
          |    account_id INTEGER NOT NULL,
          |    type TEXT NOT NULL,
          |    amount REAL NOT NULL,
          |    category TEXT NOT NULL,
          |    date TEXT NOT NULL,
          |    note TEXT NOT NULL DEFAULT ''
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE transfer (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    from_type TEXT NOT NULL,
          |    from_id INTEGER NOT NULL,
          |    to_type TEXT NOT NULL,
          |    to_id INTEGER NOT NULL,
          |    amount REAL NOT NULL,
          |    date TEXT NOT NULL,
          |    note TEXT NOT NULL DEFAULT ''
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE salary_record (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    total REAL NOT NULL,
          |    date TEXT NOT NULL,
          |    daily_fixed REAL NOT NULL,
          |    remaining REAL NOT NULL,
          |    allocation_json TEXT NOT NULL DEFAULT ''
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE investment_product (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    sub_account_id INTEGER NOT NULL,
          |    product_name TEXT NOT NULL,
          |    amount REAL NOT NULL,
          |    rate REAL NOT NULL DEFAULT 0.0,
          |    purchase_date TEXT NOT NULL,
          |    expire_date TEXT,
          |    note TEXT NOT NULL DEFAULT '',
          |    status TEXT NOT NULL DEFAULT 'ACTIVE'
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE investment_income (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    product_id INTEGER NOT NULL,
          |    amount REAL NOT NULL,
          |    income_type TEXT NOT NULL,
          |    date TEXT NOT NULL,
          |    note TEXT NOT NULL DEFAULT ''
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE stable_product (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    account_id INTEGER NOT NULL,
          |    product_name TEXT NOT NULL,
          |    product_type TEXT NOT NULL,
          |    amount REAL NOT NULL DEFAULT 0.0,
          |    annual_rate REAL NOT NULL DEFAULT 0.0,
          |    purchase_date TEXT NOT NULL,
          |    expire_date TEXT,
          |    note TEXT NOT NULL DEFAULT '',
          |    status TEXT NOT NULL DEFAULT 'ACTIVE'
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE stable_income (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    product_id INTEGER NOT NULL,
          |    amount REAL NOT NULL,
          |    date TEXT NOT NULL,
          |    note TEXT NOT NULL DEFAULT ''
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE settings (
          |    id INTEGER PRIMARY KEY DEFAULT 1,
          |    main_ratios_for_salary TEXT NOT NULL DEFAULT '{}',
          |    growth_ratios TEXT NOT NULL DEFAULT '{}',
          |    deviation_threshold REAL NOT NULL DEFAULT 0.05,
          |    daily_fixed_amount REAL NOT NULL DEFAULT 0.0,
          |    stock_asset_ratios TEXT NOT NULL DEFAULT '{}'
          |)
          """.trimMargin(), 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
