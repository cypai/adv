package com.pipai.adv.backend.battle.engine

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
                .find { it is PreviewComponent.DamagePreviewComponent }
                ?.let { (it as PreviewComponent.DamagePreviewComponent) }

        val flatAdjustment = previews.filter { it is PreviewComponent.DamageFlatAdjustmentPreviewComponent }
                .map { (it as PreviewComponent.DamageFlatAdjustmentPreviewComponent).adjustment }
                .sum()

        return baseDamage?.let { Pair(it.minDamage + flatAdjustment, it.maxDamage + flatAdjustment) }
    }

    fun calculateToHit(previews: List<PreviewComponent>): Int? {
        val toHit = previews
                .find { it is PreviewComponent.ToHitPreviewComponent }
                ?.let { (it as PreviewComponent.ToHitPreviewComponent).toHit }

        val flatAdjustment = previews.filter { it is PreviewComponent.ToHitFlatAdjustmentPreviewComponent }
                .map { (it as PreviewComponent.ToHitFlatAdjustmentPreviewComponent).adjustment }
                .sum()

        return toHit?.let { it + flatAdjustment }
    }

    fun calculateToCrit(previews: List<PreviewComponent>): Int? {
        val toCrit = previews
                .find { it is PreviewComponent.ToCritPreviewComponent }
                ?.let { (it as PreviewComponent.ToCritPreviewComponent).toCrit }

        val flatAdjustment = previews.filter { it is PreviewComponent.ToCritFlatAdjustmentPreviewComponent }
                .map { (it as PreviewComponent.ToCritFlatAdjustmentPreviewComponent).adjustment }
                .sum()

        return toCrit?.let { it + flatAdjustment }
    }
}
