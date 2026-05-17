package com.anxincaiguan

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Investment_income(
  public val id: Long,
  public val product_id: Long,
  public val amount: Double,
  public val income_type: String,
  public val date: String,
  public val note: String,
)
