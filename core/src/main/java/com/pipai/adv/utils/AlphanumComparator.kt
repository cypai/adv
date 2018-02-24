package com.pipai.adv.utils

import com.pipai.adv.tiles.PccMetadata

class AlphanumComparator : Comparator<String> {
    override fun compare(o1: String, o2: String): Int {
        return extractInt(o1) - extractInt(o2)
    }

    private fun extractInt(s: String): Int {
        val num = s.replace("\\D".toRegex(), "")
        // return 0 if no digits found
        return if (num.isEmpty()) 0 else Integer.parseInt(num)
    }
}

class PccComparator : Comparator<PccMetadata> {
    override fun compare(o1: PccMetadata, o2: PccMetadata): Int {
        return extractInt(o1.filename) - extractInt(o2.filename)
    }

    private fun extractInt(s: String): Int {
        val num = s.replace("\\D".toRegex(), "")
        // return 0 if no digits found
        return if (num.isEmpty()) 0 else Integer.parseInt(num)
    }
}
