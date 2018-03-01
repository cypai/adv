package com.pipai.adv.backend.battle.engine.rules.command

import com.pipai.adv.backend.battle.engine.*
import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.HitCritCommand
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus

class NpcMustExistRule : CommandRule {

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is ActionCommand) {
            state.npcList.getNpc(command.unitId)
                    ?: return ExecutableStatus(false, "Npc ${command.unitId} does not exist")
        }
        if (command is HitCritCommand) {
            state.npcList.getNpc(command.targetId)
                    ?: return ExecutableStatus(false, "Npc ${command.targetId} does not exist")
        }
        return ExecutableStatus.COMMAND_OK
    }
}
