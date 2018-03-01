package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.FullEnvObject
import com.pipai.adv.backend.battle.engine.*
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.MoveCommand
import com.pipai.adv.backend.battle.engine.log.MoveEvent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent

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
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache) {

        val cmd = command as MoveCommand

        val startPosition = cmd.path.first()
        val endPosition = cmd.path.last()

        val startingCell = state.battleMap.getCell(startPosition)
        val endingCell = state.battleMap.getCell(endPosition)

        val npc = startingCell.fullEnvObject as FullEnvObject.NpcEnvObject

        startingCell.fullEnvObject = null
        endingCell.fullEnvObject = npc

        state.battleLog.addEvent(MoveEvent(npc.npcId, state.npcList.getNpc(npc.npcId)!!, startPosition, endPosition))
    }
}
