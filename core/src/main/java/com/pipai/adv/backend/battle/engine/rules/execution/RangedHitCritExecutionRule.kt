package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.WeaponRange
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.HitCritCommand
import com.pipai.adv.backend.battle.engine.commands.WeaponCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.utils.MathUtils

class RangedHitCritExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand): Boolean {
        return command is ActionCommand
                && command is HitCritCommand
                && command is WeaponCommand
                && command.weapon.schema.range == WeaponRange.RANGED
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val hitCritCommand = command as HitCritCommand
        val actionCommand = command as ActionCommand

        val attackerLocation = cache.npcPositions[actionCommand.unitId]!!
        val targetLocation = cache.npcPositions[hitCritCommand.targetId]!!

        val distance = MathUtils.distance(attackerLocation.x, attackerLocation.y, targetLocation.x, targetLocation.y)
        val toHitAdjustment = when {
            distance <= 3 -> (10 * (4 - distance)).toInt()
            distance <= 5 -> (5 * (6 - distance)).toInt()
            else -> 0
        }
        val toCritAdjustment = when {
            distance <= 5 -> (5 * (5 - distance)).toInt()
            else -> 0
        }
        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        if (toHitAdjustment != 0) {
            previewComponents.add(PreviewComponent.ToHitFlatAdjustmentPreviewComponent(toHitAdjustment, "Ranged weapon to hit adjustment"))
        }
        if (toCritAdjustment != 0) {
            previewComponents.add(PreviewComponent.ToCritFlatAdjustmentPreviewComponent(toCritAdjustment, "Ranged weapon to crit adjustment"))
        }
        return previewComponents.toList()
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache) {
    }
}
