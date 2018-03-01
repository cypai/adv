package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.domain.InventoryItem

interface BattleCommand {
}

interface ActionCommand : BattleCommand {
    val unitId: Int
    val requiredAp: Int
}

interface HitCritCommand : BattleCommand {
    val targetId: Int
    val baseHit: Int
    val baseCrit: Int
}

interface WeaponCommand : BattleCommand {
    val weapon: InventoryItem.WeaponInstance
}

data class MoveCommand(override val unitId: Int, val path: List<GridPosition>) : ActionCommand {
    override val requiredAp: Int = 1
}

data class NormalAttackCommand(override val unitId: Int,
                               override val targetId: Int,
                               override val weapon: InventoryItem.WeaponInstance) : ActionCommand, HitCritCommand, WeaponCommand {
    override val requiredAp: Int = 1
    override val baseHit = 65
    override val baseCrit = 25
}
