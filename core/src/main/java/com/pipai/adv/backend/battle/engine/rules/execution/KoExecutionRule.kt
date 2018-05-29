package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.NpcKoEvent
import com.pipai.adv.backend.battle.engine.log.PlayerKoEvent

class KoExecutionRule : CommandExecutionRule {
    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return true
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        return listOf()
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {

        cache.teamNpcs[Team.PLAYER]!!.forEach { npcId ->
            val npc = state.npcList.getNpc(npcId)!!
            if (npc.unitInstance.hp <= 0
                    && !cache.currentTurnKos.contains(npcId)) {
                state.battleLog.addEvent(PlayerKoEvent(npcId, npc))
            }
        }
        cache.teamNpcs[Team.AI]!!.forEach { npcId ->
            val npc = state.npcList.getNpc(npcId)!!
            if (npc.unitInstance.hp <= 0
                    && !cache.currentTurnKos.contains(npcId)) {
                val position = cache.npcPositions[npcId]!!
                state.battleMap.getCell(position).fullEnvObject = null
                state.battleLog.addEvent(NpcKoEvent(npcId, npc))
                if (command is TargetCommand) {
                    state.battleStats.recordTargetedKill(command.unitId, npcId)
                } else {
                    state.battleStats.recordUntargetedKill(npcId)
                }
            }
        }
    }
}
