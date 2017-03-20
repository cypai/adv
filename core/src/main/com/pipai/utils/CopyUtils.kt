package com.pipai.utils

interface ShallowCopyable<T : ShallowCopyable<T>> {
    fun shallowCopy(): T
}

interface DeepCopyable<T : DeepCopyable<T>> {
    fun deepCopy(): T
}

fun <T : DeepCopyable<T>> deepCopy(list: List<List<T>>) : List<List<T>> {
    return list.map { it.map { it.deepCopy() } }
}
