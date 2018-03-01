package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.Team

class KoCannotTakeActionRule : CommandRule {

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is ActionCommand) {
            val unit = state.npcList.getNpc(command.unitId)!!
            if (unit.unitInstance.hp <= 0) {
                return ExecutableStatus(false, "This character is KOed and cannot take an action")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }
}

class KoCannotBeAttackedRule : CommandRule {

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is HitCritCommand) {
            val unit = state.npcList.getNpc(command.targetId)!!
            if (unit.unitInstance.hp <= 0) {
                return ExecutableStatus(false, "This character is KOed and cannot be attacked")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }
}

class KoExecutionRule : CommandExecutionRule {
    override fun matches(command: BattleCommand): Boolean {
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
            }
        }
    }
}
