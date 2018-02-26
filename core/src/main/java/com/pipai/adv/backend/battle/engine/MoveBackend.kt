package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.FullEnvObject.NpcEnvObject
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.npc.Npc

data class MoveCommand(override val unitId: Int, val path: List<GridPosition>) : ActionCommand {
    override val requiredAp: Int = 1
}

class MoveCommandSanityRule : CommandRule {
    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is MoveCommand) {
            val origin = command.path.first()
            val originObject = state.battleMap.getCell(origin).fullEnvObject
            if (originObject == null || originObject !is NpcEnvObject) {
                return ExecutableStatus(false, "Origin has no movable NPC")
            } else if (originObject.npcId != command.unitId) {
                return ExecutableStatus(false, "NPC at origin is not the stated NPC")
            }
            val destination = command.path.last()
            if (state.battleMap.getCell(destination).fullEnvObject != null) {
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
                         cache: BattleBackendCache): List<PreviewComponent> {

        return listOf()
    }

    override fun execute(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache) {

        val cmd = command as MoveCommand

        val startPosition = cmd.path.first()
        val endPosition = cmd.path.last()

        val startingCell = state.battleMap.getCell(startPosition)
        val endingCell = state.battleMap.getCell(endPosition)

        val npc = startingCell.fullEnvObject as NpcEnvObject

        startingCell.fullEnvObject = null
        endingCell.fullEnvObject = npc

        state.battleLog.addEvent(MoveEvent(npc.npcId, state.npcList.getNpc(npc.npcId)!!, startPosition, endPosition))
    }
}

data class MoveEvent(val npcId: Int,
                     val npc: Npc,
                     val startPosition: GridPosition,
                     val endPosition: GridPosition) : BattleLogEvent {

    override fun description() = "$npc (id $npcId) moved from $startPosition to $endPosition"
    override fun userFriendlyDescription() = "${npc.unitInstance.nickname} is moving..."
}
