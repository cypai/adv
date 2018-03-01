package com.pipai.adv.backend.battle.engine.rules.command

import com.pipai.adv.backend.battle.engine.ActionPointState
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus

class NoActionIfNotEnoughApRule : CommandRule {
    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is ActionCommand) {
            if (!hasEnoughAp(command.unitId, command.requiredAp, state.apState)) {
                return ExecutableStatus(false, "Not enough action points available")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }

    private fun hasEnoughAp(npcId: Int, requiredAp: Int, apState: ActionPointState): Boolean {
        val npcAp: Int = apState.getNpcAp(npcId)
        return npcAp >= requiredAp
    }
}