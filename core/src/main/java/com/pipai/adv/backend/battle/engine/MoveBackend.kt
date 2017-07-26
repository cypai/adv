package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.FullEnvironmentObject.NpcEnvironmentObject
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.npc.Npc

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

        val npc = startingCell.fullEnvironmentObject as NpcEnvironmentObject

        startingCell.fullEnvironmentObject = null
        endingCell.fullEnvironmentObject = npc

        unitPositions[npc.npcId] = endPosition

        state.battleLog.log.add(MoveEvent(state.npcList.getNpc(npc.npcId), startPosition, endPosition))
    }
}

data class MoveEvent(val npc: Npc,
                     val startPosition: GridPosition,
                     val endPosition: GridPosition) : BattleLogEvent {

    override fun description() = "$npc moved from $startPosition to $endPosition"
    override fun userFriendlyDescription() = "${npc.unitInstance.nickname} is moving..."
}
