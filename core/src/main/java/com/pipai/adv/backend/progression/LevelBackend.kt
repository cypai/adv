package com.pipai.adv.backend.progression

import com.pipai.adv.utils.MathUtils

class LevelBackend {
    fun expRequired(level: Int): Int {
        return 100 + 50 * MathUtils.square(level - 1)
    }
}
