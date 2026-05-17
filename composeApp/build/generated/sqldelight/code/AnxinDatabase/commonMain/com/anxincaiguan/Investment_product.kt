package com.anxincaiguan

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Investment_product(
  public val id: Long,
  public val sub_account_id: Long,
  public val product_name: String,
  public val amount: Double,
  public val rate: Double,
  public val purchase_date: String,
  public val expire_date: String?,
  public val note: String,
  public val status: String,
)
