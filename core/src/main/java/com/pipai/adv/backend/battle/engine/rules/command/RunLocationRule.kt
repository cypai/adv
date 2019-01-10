package com.pipai.adv.backend.battle.engine.rules.command

import com.pipai.adv.backend.battle.domain.BattleMapCellSpecialFlag
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.RunCommand
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus

class RunLocationRule : CommandRule {

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is RunCommand) {
            val npc = cache.getNpcPosition(command.unitId)!!
            return if (state.battleMap.getCell(npc).specialFlags.any { it is BattleMapCellSpecialFlag.Exit }) {
                ExecutableStatus.COMMAND_OK
            } else {
                ExecutableStatus(false, "Not on exit tile")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }

}
