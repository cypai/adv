package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.calculators.CritCalculator
import com.pipai.adv.backend.battle.engine.calculators.DamageCalculator
import com.pipai.adv.backend.battle.engine.calculators.HitCalculator
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.DamagePreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToCritPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToHitPreviewComponent
import com.pipai.adv.backend.battle.engine.log.DamageEvent
import com.pipai.adv.backend.battle.engine.log.DamageOutcome
import com.pipai.adv.utils.RNG

/**
 * This execution rule should be after everything else is evaluated to calculate damage correctly.
 */
class AttackCalculationExecutionRule(private val hitCalculator: HitCalculator,
                                     private val critCalculator: CritCalculator,
                                     private val damageCalculator: DamageCalculator) : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is TargetCommand
                && previews.any { it is ToHitPreviewComponent }
                && previews.any { it is ToCritPreviewComponent }
                && previews.any { it is DamagePreviewComponent }
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

        val cmd = command as TargetCommand
        val damageRange = damageCalculator.calculateDamageRange(previews)
                ?: throw IllegalStateException("Damage range unavailable")
        val toHit = hitCalculator.calculateToHit(previews)
                ?: throw IllegalStateException("To hit unavailable")
        val toCrit = critCalculator.calculateToCrit(previews)
                ?: throw IllegalStateException("To crit unavailable")

        val hitRoll = RNG.nextInt(100)
        var damage: Int
        val target = state.npcList.get(cmd.targetId)!!
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

            state.battleLog.addEvent(DamageEvent(cmd.targetId, target.deepCopy(), damage, if (crit) DamageOutcome.CRIT else DamageOutcome.HIT))
        } else {
            state.battleLog.addEvent(DamageEvent(cmd.targetId, target.deepCopy(), 0, DamageOutcome.MISS))
        }
    }
}
