package com.pipai.adv.backend.battle.domain

import com.pipai.utils.DeepCopyable

data class BattleUnit(val unitInstance: UnitInstance) : DeepCopyable<BattleUnit> {
    override fun deepCopy(): BattleUnit {
        return copy(unitInstance.deepCopy())
    }
}
