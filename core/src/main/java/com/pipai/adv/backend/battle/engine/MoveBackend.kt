package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.BattleUnit
import com.pipai.adv.backend.battle.domain.FullEnvironmentObject.BattleUnitEnvironmentObject
import com.pipai.adv.backend.battle.domain.GridPosition

data class MoveCommand(val unitId: Int, val path: List<GridPosition>) : BattleCommand

class NoMovingToFullCellRule : CommandRule {
    override fun canBeExecuted(command: BattleCommand, state: BattleState, unitPositions: Map<Int, GridPosition>): ExecutableStatus {
        if (command is MoveCommand) {
            val destination = command.path.last()
            if (state.battleMap.getCell(destination).fullEnvironmentObject != null) {
                return ExecutableStatus(false, "Destination is not empty")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }
}

class MovementExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand): Boolean {
        return command is MoveCommand
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         unitPositions: Map<Int, GridPosition>): List<PreviewComponent> {

        return listOf()
    }

    override fun execute(command: BattleCommand,
                         state: BattleState,
                         unitPositions: MutableMap<Int, GridPosition>) {

        val cmd = command as MoveCommand

        val startPosition = cmd.path.first()
        val endPosition = cmd.path.last()

        val startingCell = state.battleMap.getCell(startPosition)
        val endingCell = state.battleMap.getCell(endPosition)

        val unitEnvObj = startingCell.fullEnvironmentObject as BattleUnitEnvironmentObject

        startingCell.fullEnvironmentObject = null
        endingCell.fullEnvironmentObject = unitEnvObj

        unitPositions[unitEnvObj.battleUnit.id] = endPosition

        state.battleLog.log.add(MoveEvent(unitEnvObj.battleUnit, startPosition, endPosition))
    }
}

data class MoveEvent(val battleUnit: BattleUnit,
                     val startPosition: GridPosition,
                     val endPosition: GridPosition) : BattleLogEvent {

    override fun description() = "$battleUnit moved from $startPosition to $endPosition"
    override fun userFriendlyDescription() = "${battleUnit.unitInstance.nickname} is moving..."
}
