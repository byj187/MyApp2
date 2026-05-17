package com.anxincaiguan.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.anxincaiguan.AnxinDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(AnxinDatabase.Schema, "anxincaiguan.db")
    }
}
