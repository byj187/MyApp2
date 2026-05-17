package com.anxincaiguan

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Transfer(
  public val id: Long,
  public val from_type: String,
  public val from_id: Long,
  public val to_type: String,
  public val to_id: Long,
  public val amount: Double,
  public val date: String,
  public val note: String,
)
