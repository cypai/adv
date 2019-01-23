package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.domain.AmmoChangePreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.AmmoChangeEvent

class AmmoChangeExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return previews.any { it is AmmoChangePreviewComponent }
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

        val ammoChangeComponent = previews
                .find { it is AmmoChangePreviewComponent }
                ?.let { it as AmmoChangePreviewComponent }

        if (ammoChangeComponent != null) {
            state.npcList.get(ammoChangeComponent.npcId)!!.unitInstance.weapon?.ammo = ammoChangeComponent.newAmount
            state.battleLog.addEvent(AmmoChangeEvent(ammoChangeComponent.npcId, ammoChangeComponent.newAmount))
        }
    }
}
