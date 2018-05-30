package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommand
import com.pipai.adv.backend.battle.engine.domain.*
import com.pipai.adv.backend.battle.engine.log.NormalAttackEvent

class NormalAttackExecutionRule : CommandExecutionRule {

    companion object {
        const val DAMAGE_RANGE = 2
    }

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is NormalAttackCommand
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as NormalAttackCommand
        val base = state.npcList.getNpc(cmd.unitId)!!.unitInstance.stats.strength + state.getNpcWeapon(cmd.unitId)!!.schema.patk

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        previewComponents.add(ToHitPreviewComponent(65))
        previewComponents.add(ToCritPreviewComponent(25))
        previewComponents.add(DamagePreviewComponent(base - DAMAGE_RANGE, base + DAMAGE_RANGE))
        previewComponents.add(ApUsedPreviewComponent(cmd.unitId, state.apState.getNpcAp(cmd.unitId)))

        return previewComponents.toList()
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {

        val cmd = command as NormalAttackCommand
        state.battleLog.addEvent(NormalAttackEvent(
                cmd.unitId,
                state.getNpc(cmd.unitId)!!,
                cmd.targetId,
                state.getNpc(cmd.targetId)!!,
                state.getNpcWeapon(cmd.unitId)!!))
    }
}
