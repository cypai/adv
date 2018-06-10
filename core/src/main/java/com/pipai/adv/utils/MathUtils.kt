package com.pipai.adv.utils

fun Float.clamp(min: Float, max: Float): Float = Math.max(min, Math.min(this, max))
fun Double.clamp(min: Double, max: Double): Double = Math.max(min, Math.min(this, max))
fun Int.clamp(min: Int, max: Int): Int = Math.max(min, Math.min(this, max))

object MathUtils {
    fun clamp(value: Float, min: Float, max: Float): Float = Math.max(min, Math.min(value, max))
    fun clamp(value: Double, min: Double, max: Double): Double = Math.max(min, Math.min(value, max))
    fun clamp(value: Int, min: Int, max: Int): Int = Math.max(min, Math.min(value, max))

    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        return Math.sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble())
    }

    fun distance(x1: Int, y1: Int, x2: Int, y2: Int): Double {
        return Math.sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble())
    }

    fun distance2(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)
    }

    fun distance2(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)
    }

    fun square(x: Int): Int {
        return x * x
    }

    fun <T> randomSelect(list: List<T>): T {
        return list[RNG.nextInt(list.size)]
    }
}
