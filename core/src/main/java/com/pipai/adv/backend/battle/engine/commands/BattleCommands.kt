package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.engine.domain.TargetStagePreviewComponent
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

data class TargetSkillCommand(val skill: UnitSkill,
                              override val unitId: Int,
                              override val targetId: Int) : ActionCommand, TargetCommand

data class DefendCommand(override val unitId: Int) : ActionCommand

data class WaitCommand(override val unitId: Int) : ActionCommand

data class TargetStageExecuteCommand(val preview: TargetStagePreviewComponent) : ActionCommand, TargetCommand {
    override val unitId: Int = preview.unitId
    override val targetId: Int = preview.targetId
}
