package com.pipai.adv.backend.battle.engine.rules.command

import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus

class NpcMustExistRule : CommandRule {

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is ActionCommand) {
            state.npcList.get(command.unitId)
                    ?: return ExecutableStatus(false, "Npc ${command.unitId} does not exist")
        }
        if (command is TargetCommand) {
            state.npcList.get(command.targetId)
                    ?: return ExecutableStatus(false, "Npc ${command.targetId} does not exist")
        }
        return ExecutableStatus.COMMAND_OK
    }
}
