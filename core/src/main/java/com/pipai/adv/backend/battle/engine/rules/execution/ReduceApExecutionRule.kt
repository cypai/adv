package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent

class ReduceApExecutionRule : CommandExecutionRule {
    override fun matches(command: BattleCommand): Boolean {
        return command is ActionCommand
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
        if (command is ActionCommand) {
            val newAp = state.apState.getNpcAp(command.unitId) - command.requiredAp
            state.apState.setNpcAp(command.unitId, newAp)
        }
    }
}
