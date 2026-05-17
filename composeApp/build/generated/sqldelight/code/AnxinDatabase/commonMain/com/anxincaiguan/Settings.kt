package com.anxincaiguan

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Settings(
  public val id: Long,
  public val main_ratios_for_salary: String,
  public val growth_ratios: String,
  public val deviation_threshold: Double,
  public val daily_fixed_amount: Double,
  public val stock_asset_ratios: String,
)
