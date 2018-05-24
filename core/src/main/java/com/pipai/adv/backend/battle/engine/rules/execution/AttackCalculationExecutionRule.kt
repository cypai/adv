package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.*
import com.pipai.adv.backend.battle.engine.log.DamageEvent
import com.pipai.adv.backend.battle.engine.log.DamageOutcome
import com.pipai.adv.utils.RNG

/**
 * This execution rule should be after everything else is evaluated to calculate damage correctly.
 */
class AttackCalculationExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is TargetCommand
                && previews.any { it is ToHitPreviewComponent }
                && previews.any { it is ToCritPreviewComponent }
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        return listOf()
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {

        val cmd = command as TargetCommand
        val damageRange = calculateDamageRange(previews)
        val toHit = calculateToHit(previews)
        val toCrit = calculateToCrit(previews)

        if (damageRange != null && toHit != null && toCrit != null) {
            val hitRoll = RNG.nextInt(100)
            var damage: Int
            val target = state.npcList.getNpc(cmd.targetId)!!
            if (hitRoll < toHit) {
                val critRoll = RNG.nextInt(100)
                val crit = critRoll < toCrit

                damage = RNG.nextInt(damageRange.second - damageRange.first + 1) + damageRange.first
                if (crit) {
                    damage += damage / 2
                }

                target.unitInstance.hp -= damage
                if (target.unitInstance.hp < 0) {
                    target.unitInstance.hp = 0
                }

                state.battleLog.addEvent(DamageEvent(cmd.targetId, target, damage, if (crit) DamageOutcome.CRIT else DamageOutcome.HIT))
            } else {
                state.battleLog.addEvent(DamageEvent(cmd.targetId, target, 0, DamageOutcome.MISS))
            }
        }
    }

    fun calculateDamageRange(previews: List<PreviewComponent>): Pair<Int, Int>? {
        val damageComponents = damageRangeComponents(previews)
        return if (damageComponents == null) {
            null
        } else {
            val baseDamage = damageComponents.first
            val flatAdjustment = damageComponents.second
                    .filter { it is DamageFlatAdjustmentPreviewComponent }
                    .sumBy { (it as DamageFlatAdjustmentPreviewComponent).adjustment }

            val baseRange = Pair(baseDamage.minDamage + flatAdjustment, baseDamage.maxDamage + flatAdjustment)

            val scaleAdjustment = damageComponents.second
                    .filter { it is DamageScaleAdjustmentPreviewComponent }
                    .sumBy { (it as DamageScaleAdjustmentPreviewComponent).adjustment } / 100f

            Pair(
                    baseRange.first + (baseRange.first * scaleAdjustment).toInt(),
                    baseRange.second + (baseRange.second * scaleAdjustment).toInt())
        }
    }

    fun damageRangeComponents(previews: List<PreviewComponent>): Pair<DamagePreviewComponent, List<PreviewComponent>>? {
        val baseDamage = previews
                .find { it is DamagePreviewComponent }
                ?.let { (it as DamagePreviewComponent) }

        return if (baseDamage == null) {
            null
        } else {
            val adjustments = previews.filter { it is DamageFlatAdjustmentPreviewComponent || it is DamageScaleAdjustmentPreviewComponent }
            Pair(baseDamage, adjustments)
        }
    }

    fun calculateToHit(previews: List<PreviewComponent>): Int? {
        val toHit = toHitComponents(previews)
        return if (toHit == null) {
            null
        } else {
            val baseToHit = toHit.first.toHit
            val flatAdjustment = toHit.second.sumBy { (it as ToHitFlatAdjustmentPreviewComponent).adjustment }
            baseToHit + flatAdjustment
        }
    }

    fun toHitComponents(previews: List<PreviewComponent>): Pair<ToHitPreviewComponent, List<PreviewComponent>>? {
        val toHit = previews
                .find { it is ToHitPreviewComponent }
                ?.let { (it as ToHitPreviewComponent) }

        return if (toHit == null) {
            null
        } else {
            val flatAdjustment = previews.filter { it is ToHitFlatAdjustmentPreviewComponent }
            Pair(toHit, flatAdjustment)
        }
    }

    fun calculateToCrit(previews: List<PreviewComponent>): Int? {
        val toCrit = toCritComponents(previews)
        return if (toCrit == null) {
            null
        } else {
            val baseToCrit = toCrit.first.toCrit
            val flatAdjustment = toCrit.second.sumBy { (it as ToCritFlatAdjustmentPreviewComponent).adjustment }
            baseToCrit + flatAdjustment
        }
    }

    fun toCritComponents(previews: List<PreviewComponent>): Pair<ToCritPreviewComponent, List<PreviewComponent>>? {
        val toCrit = previews
                .find { it is ToCritPreviewComponent }
                ?.let { (it as ToCritPreviewComponent) }

        return if (toCrit == null) {
            null
        } else {
            val flatAdjustment = previews.filter { it is ToCritFlatAdjustmentPreviewComponent }
            Pair(toCrit, flatAdjustment)
        }
    }
}
