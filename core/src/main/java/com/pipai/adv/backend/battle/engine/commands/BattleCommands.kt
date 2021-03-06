package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.engine.domain.TargetStagePreviewComponent
import com.pipai.adv.domain.UnitSkill

interface BattleCommand {
}

interface ActionCommand : BattleCommand {
    val unitId: Int
}

interface TargetCommand : ActionCommand {
    val targetId: Int
}

interface PositionCommand : ActionCommand {
    val position: GridPosition
}

data class MoveCommand(override val unitId: Int, val path: List<GridPosition>) : ActionCommand

data class RunCommand(override val unitId: Int) : ActionCommand

data class NormalAttackCommand(override val unitId: Int,
                               override val targetId: Int) : ActionCommand, TargetCommand

data class TargetSkillCommand(val skill: UnitSkill,
                              override val unitId: Int,
                              override val targetId: Int) : ActionCommand, TargetCommand

data class SkillTpCheckCommand(val skill: UnitSkill,
                               override val unitId: Int) : ActionCommand

data class DefendCommand(override val unitId: Int) : ActionCommand

data class WaitCommand(override val unitId: Int) : ActionCommand

data class InteractCommand(override val unitId: Int,
                           override val position: GridPosition) : ActionCommand, PositionCommand

data class TargetStageExecuteCommand(val preview: TargetStagePreviewComponent) : ActionCommand, TargetCommand {
    override val unitId: Int = preview.unitId
    override val targetId: Int = preview.targetId
}
