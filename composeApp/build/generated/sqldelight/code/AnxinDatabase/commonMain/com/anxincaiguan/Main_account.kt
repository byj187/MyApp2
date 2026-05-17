package com.anxincaiguan

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Main_account(
  public val id: Long,
  public val name: String,
  public val balance: Double,
  public val target_ratio_for_salary: Double,
)
