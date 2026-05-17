package com.anxincaiguan

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Bill(
  public val id: Long,
  public val account_type: String,
  public val account_id: Long,
  public val type: String,
  public val amount: Double,
  public val category: String,
  public val date: String,
  public val note: String,
)
