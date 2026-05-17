package com.anxincaiguan

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.anxincaiguan.composeApp.newInstance
import com.anxincaiguan.composeApp.schema
import kotlin.Unit

public interface AnxinDatabase : Transacter {
  public val anxinDatabaseQueries: AnxinDatabaseQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = AnxinDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): AnxinDatabase =
        AnxinDatabase::class.newInstance(driver)
  }
}
