package com.pipai.adv.utils

fun Float.clamp(min: Float, max: Float): Float = Math.max(min, Math.min(this, max))
fun Double.clamp(min: Double, max: Double): Double = Math.max(min, Math.min(this, max))
fun Int.clamp(min: Int, max: Int): Int = Math.max(min, Math.min(this, max))

object MathUtils {
    fun clamp(value: Float, min: Float, max: Float): Float = Math.max(min, Math.min(value, max))
    fun clamp(value: Double, min: Double, max: Double): Double = Math.max(min, Math.min(value, max))
    fun clamp(value: Int, min: Int, max: Int): Int = Math.max(min, Math.min(value, max))
}
