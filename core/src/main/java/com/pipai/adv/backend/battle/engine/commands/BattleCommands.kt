package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.classes.skills.UnitSkill

interface BattleCommand {
}

interface ActionCommand : BattleCommand {
    val unitId: Int
}

interface TargetCommand : ActionCommand {
    val targetId: Int
}

data class MoveCommand(override val unitId: Int, val path: List<GridPosition>) : ActionCommand

data class NormalAttackCommand(override val unitId: Int,
                               override val targetId: Int) : ActionCommand, TargetCommand

data class SkillCommand(val skill: UnitSkill, val command: ActionCommand) : ActionCommand {
    override val unitId: Int = command.unitId
}

data class DefendCommand(override val unitId: Int) : ActionCommand

data class WaitCommand(override val unitId: Int) : ActionCommand
