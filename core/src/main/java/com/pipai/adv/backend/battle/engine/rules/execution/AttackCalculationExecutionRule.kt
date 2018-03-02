package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.*
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.HitCritCommand
import com.pipai.adv.backend.battle.engine.domain.*
import com.pipai.adv.backend.battle.engine.log.DamageEvent
import com.pipai.adv.backend.battle.engine.log.DamageOutcome
import com.pipai.adv.utils.RNG

/**
 * This execution rule should be after everything else is evaluated to calculate damage correctly.
 */
class AttackCalculationExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand): Boolean {
        return command is HitCritCommand
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

        val cmd = command as HitCritCommand
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
        val baseDamage = previews
                .find { it is DamagePreviewComponent }
                ?.let { (it as DamagePreviewComponent) }

        val flatAdjustment = previews.filter { it is DamageFlatAdjustmentPreviewComponent }
                .map { (it as DamageFlatAdjustmentPreviewComponent).adjustment }
                .sum()

        return baseDamage?.let { Pair(it.minDamage + flatAdjustment, it.maxDamage + flatAdjustment) }
    }

    fun calculateToHit(previews: List<PreviewComponent>): Int? {
        val toHit = previews
                .find { it is ToHitPreviewComponent }
                ?.let { (it as ToHitPreviewComponent).toHit }

        val flatAdjustment = previews.filter { it is ToHitFlatAdjustmentPreviewComponent }
                .map { (it as ToHitFlatAdjustmentPreviewComponent).adjustment }
                .sum()

        return toHit?.let { it + flatAdjustment }
    }

    fun calculateToCrit(previews: List<PreviewComponent>): Int? {
        val toCrit = previews
                .find { it is ToCritPreviewComponent }
                ?.let { (it as ToCritPreviewComponent).toCrit }

        val flatAdjustment = previews.filter { it is ToCritFlatAdjustmentPreviewComponent }
                .map { (it as ToCritFlatAdjustmentPreviewComponent).adjustment }
                .sum()

        return toCrit?.let { it + flatAdjustment }
    }
}
