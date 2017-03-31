package com.pipai.adv.backend.battle.domain

import com.pipai.utils.DeepCopyable

data class BattleUnit(val unitInstance: UnitInstance, val id: Int) : DeepCopyable<BattleUnit> {
    override fun deepCopy() = copy(unitInstance.deepCopy())
}
