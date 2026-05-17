package com.anxincaiguan

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Salary_record(
  public val id: Long,
  public val total: Double,
  public val date: String,
  public val daily_fixed: Double,
  public val remaining: Double,
  public val allocation_json: String,
)
