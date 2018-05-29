package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.*
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.DevHpChangeCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.DamageEvent
import com.pipai.adv.backend.battle.engine.log.DamageOutcome
import com.pipai.adv.backend.battle.engine.log.HealEvent

class DevHpChangeExecutionRule : CommandExecutionRule {
    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is DevHpChangeCommand
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

        val cmd = command as DevHpChangeCommand
        val npc = state.npcList.getNpc(cmd.unitId)!!
        val damage = npc.unitInstance.hp - cmd.hp
        npc.unitInstance.hp = cmd.hp
        when {
            damage > 0 -> state.battleLog.addEvent(DamageEvent(cmd.unitId, npc, damage, DamageOutcome.HIT))
            damage < 0 -> state.battleLog.addEvent(HealEvent(cmd.unitId, npc, -damage))
        }
    }
}
