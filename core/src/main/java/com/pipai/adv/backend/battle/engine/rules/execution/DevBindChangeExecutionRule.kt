package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.DevBindChangeCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.BindEvent
import com.pipai.adv.backend.battle.engine.log.UnbindEvent

class DevBindChangeExecutionRule : CommandExecutionRule {
    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is DevBindChangeCommand
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

        val cmd = command as DevBindChangeCommand
        val npc = state.npcList.getNpc(cmd.unitId)!!
        state.npcStatusState.setNpcBind(cmd.unitId, cmd.bodyPart, cmd.amount)
        val bindState = state.npcStatusState.getNpcBind(cmd.unitId, cmd.bodyPart)
        when (bindState) {
            0 -> state.battleLog.addEvent(UnbindEvent(cmd.unitId, npc, cmd.bodyPart))
            else -> state.battleLog.addEvent(BindEvent(cmd.unitId, npc, cmd.bodyPart, cmd.amount))
        }
    }
}
