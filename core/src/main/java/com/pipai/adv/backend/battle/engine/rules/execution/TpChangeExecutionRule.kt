package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.domain.TpUsedPreviewComponent
import com.pipai.adv.backend.battle.engine.log.TpChangeEvent

class TpChangeExecutionRule : CommandExecutionRule {
    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return previews.any { it is TpUsedPreviewComponent }
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        return listOf()
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {

        previews.forEach {
            if (it is TpUsedPreviewComponent) {
                val unitInstance = state.npcList.get(it.npcId)!!.unitInstance
                val previousTp = unitInstance.tp
                val newTp = previousTp - it.tpUsed
                unitInstance.tp = newTp
                state.battleLog.addEvent(TpChangeEvent(it.npcId, newTp))
            }
        }
    }
}
