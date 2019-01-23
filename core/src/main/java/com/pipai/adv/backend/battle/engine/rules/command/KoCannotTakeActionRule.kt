package com.pipai.adv.backend.battle.engine.rules.command

import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus

class KoCannotTakeActionRule : CommandRule {

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is ActionCommand) {
            val unit = state.npcList.get(command.unitId)!!
            if (unit.unitInstance.hp <= 0) {
                return ExecutableStatus(false, "This character is KOed and cannot take an action")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }
}
