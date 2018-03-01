package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.WeaponAttribute
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommand
import com.pipai.adv.backend.battle.engine.log.NormalAttackEvent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent

class NormalAttackExecutionRule : CommandExecutionRule {

    companion object {
        const val DAMAGE_RANGE = 2
    }

    override fun matches(command: BattleCommand): Boolean {
        return command is NormalAttackCommand
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as NormalAttackCommand
        val base = state.npcList.getNpc(cmd.unitId)!!.unitInstance.schema.baseStats.strength + cmd.weapon.schema.patk

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        previewComponents.add(PreviewComponent.DamagePreviewComponent(base - DAMAGE_RANGE, base + DAMAGE_RANGE))

        if (cmd.weapon.schema.attributes.contains(WeaponAttribute.CAN_FAST_RELOAD)
                || cmd.weapon.schema.attributes.contains(WeaponAttribute.CAN_RELOAD)) {

            previewComponents.add(PreviewComponent.AmmoChangePreviewComponent(cmd.unitId, cmd.weapon.ammo - 1))
        }

        return previewComponents.toList()
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache) {

        val cmd = command as NormalAttackCommand
        state.battleLog.addEvent(NormalAttackEvent(
                cmd.unitId,
                state.npcList.getNpc(cmd.unitId)!!,
                cmd.targetId,
                state.npcList.getNpc(cmd.unitId)!!,
                cmd.weapon))
    }
}
