package com.pipai.adv.backend.battle.engine.rules.command

import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommand
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus

class KoCannotBeAttackedRule : CommandRule {

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is NormalAttackCommand) {
            val unit = state.npcList.get(command.targetId)!!
            if (unit.unitInstance.hp <= 0) {
                return ExecutableStatus(false, "This character is KOed and cannot be attacked")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }
}

