package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.AmmoChangeEvent

class AmmoChangeExecutionRule : CommandExecutionRule {

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

        val ammoChangeComponent = previews
                .find { it is PreviewComponent.AmmoChangePreviewComponent }
                ?.let { it as PreviewComponent.AmmoChangePreviewComponent }

        if (ammoChangeComponent != null) {
            state.npcList.getNpc(ammoChangeComponent.npcId)!!.unitInstance.weapon?.ammo = ammoChangeComponent.newAmount
            state.battleLog.addEvent(AmmoChangeEvent(ammoChangeComponent.npcId, ammoChangeComponent.newAmount))
        }
    }
}
