package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.domain.ApUsedPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.ApChangeEvent

class ChangeApExecutionRule : CommandExecutionRule {
    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return true
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

        previews.forEach {
            if (it is ApUsedPreviewComponent) {
                val previousAp = state.apState.getNpcAp(it.npcId)
                val newAp = previousAp - it.apUsed
                state.apState.setNpcAp(it.npcId, newAp)
                state.battleLog.addEvent(ApChangeEvent(it.npcId, newAp))
            }
        }
    }
}
