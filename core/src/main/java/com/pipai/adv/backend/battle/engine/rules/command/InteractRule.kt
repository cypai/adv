package com.pipai.adv.backend.battle.engine.rules.command

import com.pipai.adv.backend.battle.domain.FullEnvObject
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.InteractCommand
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus
import com.pipai.adv.utils.GridUtils

class InteractRule : CommandRule {

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is InteractCommand) {
            val npcPosition = cache.getNpcPosition(command.unitId)!!
            if (!GridUtils.isNeighbor(npcPosition, command.position)) {
                return ExecutableStatus(false, "Not next to the interaction location")
            }
            val cell = state.battleMap.getCell(command.position)
            if (cell.fullEnvObject !is FullEnvObject.ChestEnvObject) {
                return ExecutableStatus(false, "Nothing to interact with")
            }
            val npc = state.getNpc(command.unitId)!!
            if (npc.unitInstance.inventory.all { it.item != null }) {
                return ExecutableStatus(false, "Their inventory is full")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }

}
