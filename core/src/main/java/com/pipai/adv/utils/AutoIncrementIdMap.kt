package com.pipai.adv.utils

import com.fasterxml.jackson.annotation.JsonIgnore

class AutoIncrementIdMap<T> : Iterable<Map.Entry<Int, T>>, ShallowCopyable<AutoIncrementIdMap<T>> {

    private val items: MutableMap<Int, T> = mutableMapOf()

    @Deprecated("Just for jackson to serialize properly, use getAll instead")
    fun getItems(): Map<Int, T> {
        return items.toMap()
    }

    @JsonIgnore
    fun getAll(): Map<Int, T> {
        return items.toMap()
    }

    fun size(): Int = items.size

    override operator fun iterator(): Iterator<Map.Entry<Int, T>> {
        return items.asIterable().iterator()
    }

    fun add(item: T): Int {
        val id = (items.keys.max() ?: -1) + 1
        items[id] = item
        return id
    }

    fun set(item: T, id: Int) {
        items[id] = item
    }

    fun remove(id: Int) {
        if (exists(id)) {
            items.remove(id)
        }
    }

    fun clear() {
        items.clear()
    }

    fun exists(id: Int): Boolean = items.containsKey(id)

    fun get(id: Int): T? {
        return items[id]
    }

    override fun shallowCopy(): AutoIncrementIdMap<T> {
        val copy = AutoIncrementIdMap<T>()
        items.forEach { copy.set(it.value, it.key) }
        return copy
    }
}

fun <T> Int?.fetch(idMap: AutoIncrementIdMap<T>): T? {
    return this?.let { idMap.get(it) }
}
