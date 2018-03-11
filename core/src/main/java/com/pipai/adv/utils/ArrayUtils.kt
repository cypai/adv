package com.pipai.adv.utils

import com.badlogic.gdx.utils.Array

object ArrayUtils {
    fun <T> iterableToArray(iterable: Iterable<T>): Array<T> {
        val arr = Array<T>()
        iterable.forEach { arr.add(it) }
        return arr
    }

    fun <T> libgdxArrayOf(vararg elements: T): Array<T> {
        val arr = Array<T>()
        elements.forEach { arr.add(it) }
        return arr
    }
}
