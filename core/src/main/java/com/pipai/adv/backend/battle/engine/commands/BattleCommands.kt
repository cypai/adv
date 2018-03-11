package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.domain.InventoryItem

interface BattleCommand {
}

interface ActionCommand : BattleCommand {
    val unitId: Int
}

interface TargetCommand : ActionCommand {
    val targetId: Int
}

interface HitCritCommand : TargetCommand {
    val baseHit: Int
    val baseCrit: Int
}

interface WeaponCommand : BattleCommand {
    val weapon: InventoryItem.WeaponInstance
}

data class MoveCommand(override val unitId: Int, val path: List<GridPosition>) : ActionCommand

data class NormalAttackCommand(override val unitId: Int,
                               override val targetId: Int,
                               override val weapon: InventoryItem.WeaponInstance) : HitCritCommand, WeaponCommand {
    override val baseHit = 65
    override val baseCrit = 25
}

data class DefendCommand(override val unitId: Int) : ActionCommand

data class WaitCommand(override val unitId: Int) : ActionCommand
