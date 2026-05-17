package com.anxincaiguan

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Stable_product(
  public val id: Long,
  public val account_id: Long,
  public val product_name: String,
  public val product_type: String,
  public val amount: Double,
  public val annual_rate: Double,
  public val purchase_date: String,
  public val expire_date: String?,
  public val note: String,
  public val status: String,
)
