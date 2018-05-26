package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.WeaponRange
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.*
import com.pipai.adv.utils.MathUtils

class RangedHitCritExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is ActionCommand
                && command is TargetCommand
                && previews.any { it is ToHitPreviewComponent }
                && previews.any { it is ToCritPreviewComponent }
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val actionCommand = command as ActionCommand
        val targetCommand = command as TargetCommand

        if (state.getNpcWeapon(actionCommand.unitId)!!.schema.range == WeaponRange.RANGED) {
            val attackerLocation = cache.npcPositions[actionCommand.unitId]!!
            val targetLocation = cache.npcPositions[targetCommand.targetId]!!

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
                previewComponents.add(ToHitFlatAdjustmentPreviewComponent(toHitAdjustment, "Range"))
            }
            if (toCritAdjustment != 0) {
                previewComponents.add(ToCritFlatAdjustmentPreviewComponent(toCritAdjustment, "Range"))
            }
            return previewComponents.toList()
        } else {
            return listOf()
        }
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {
    }
}
