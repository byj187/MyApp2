package com.anxincaiguan.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

fun fmt2(v: Double): String {
    val sign = if (v < 0) "-" else ""
    val absV = abs(v)
    val whole = absV.toLong()
    val frac = ((absV - whole) * 100 + 0.5).toLong().coerceAtMost(99)
    val fs = if (frac < 10) "0$frac" else "$frac"
    return sign + whole.toString() + "." + fs
}

fun fmt1(v: Double): String {
    val sign = if (v < 0) "-" else ""
    val absV = abs(v)
    val rounded = ((absV * 10) + 0.5).toLong()
    val intPart = rounded / 10
    val decPart = (rounded % 10).toString()
    return sign + intPart.toString() + "." + decPart
}

fun fmt0(v: Double): String = ((v * 100 + 0.5).toLong() / 100).toString()

val Blue600 = Color(0xFF1A73E8)
val Blue500 = Color(0xFF4285F4)
val Blue400 = Color(0xFF5E97F6)
val Blue50 = Color(0xFFE8F0FE)

val Orange500 = Color(0xFFFF6D00)
val Orange400 = Color(0xFFFF8F00)
val Orange50 = Color(0xFFFFF3E0)

val DailyColor = Color(0xFF4CAF50)
val QualityColor = Color(0xFFFF9800)
val StableColor = Color(0xFF2196F3)
val GrowthColor = Color(0xFF9C27B0)

val IndexFundColor = Color(0xFFE91E63)
val ActiveEquityColor = Color(0xFFFF5722)
val BondColor = Color(0xFF009688)
val AlternativeColor = Color(0xFF607D8B)

val SuccessColor = Color(0xFF4CAF50)
val WarningColor = Color(0xFFFF9800)
val ErrorColor = Color(0xFFF44336)

val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray400 = Color(0xFFBDBDBD)
val Gray500 = Color(0xFF9E9E9E)
val Gray600 = Color(0xFF757575)
val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF424242)
val Gray900 = Color(0xFF212121)

val BackgroundLight = Color(0xFFF8F9FA)
val SurfaceLight = Color(0xFFFFFFFF)
