package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.WeaponRange
import com.pipai.adv.utils.MathUtils

class BaseHitCritExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand): Boolean {
        return command is HitCritCommand
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as HitCritCommand
        return listOf(
                PreviewComponent.ToHitPreviewComponent(cmd.baseHit),
                PreviewComponent.ToCritPreviewComponent(cmd.baseCrit))
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache) {
    }
}

class MeleeHitCritExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand): Boolean {
        return command is HitCritCommand && command is WeaponCommand && command.weapon.schema.range == WeaponRange.MELEE
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        return listOf(
                PreviewComponent.ToHitFlatAdjustmentPreviewComponent(30, "Melee weapon to hit bonus"),
                PreviewComponent.ToCritFlatAdjustmentPreviewComponent(25, "Melee weapon to crit bonus"))
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache) {
    }
}

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
