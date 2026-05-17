package com.anxincaiguan

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Stable_income(
  public val id: Long,
  public val product_id: Long,
  public val amount: Double,
  public val date: String,
  public val note: String,
)
