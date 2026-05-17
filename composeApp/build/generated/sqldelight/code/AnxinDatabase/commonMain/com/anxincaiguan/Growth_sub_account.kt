package com.anxincaiguan

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Growth_sub_account(
  public val id: Long,
  public val name: String,
  public val idle_amount: Double,
  public val invested_amount: Double,
  public val target_ratio: Double,
)
