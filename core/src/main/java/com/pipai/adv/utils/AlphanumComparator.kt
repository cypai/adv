package com.pipai.adv.utils

import com.pipai.adv.tiles.PccMetadata

class AlphanumComparator<T>(private val mapper: (T) -> String) : Comparator<T> {
    override fun compare(o1: T, o2: T): Int {
        return extractInt(mapper.invoke(o1)) - extractInt(mapper.invoke(o2))
    }

    private fun extractInt(s: String): Int {
        val num = s.replace("\\D".toRegex(), "")
        // return 0 if no digits found
        return if (num.isEmpty()) 0 else Integer.parseInt(num)
    }
}
