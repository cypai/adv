package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.HealPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.HealScaleAdjustmentPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.HealEvent
import com.pipai.adv.utils.RNG

/**
 * This execution rule should be after everything else is evaluated to calculate healing correctly.
 */
class HealExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is TargetCommand
                && previews.any { it is HealPreviewComponent }
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

        val target = state.npcList.getNpc(cmd.targetId)!!

        val healRange = calculateHealRange(previews)

        if (healRange != null) {
            val heal = RNG.nextInt(healRange.second - healRange.first + 1) + healRange.first

            target.unitInstance.hp += heal
            if (target.unitInstance.hp > target.unitInstance.stats.hpMax) {
                target.unitInstance.hp = target.unitInstance.stats.hpMax
            }

            state.battleLog.addEvent(HealEvent(cmd.targetId, target.deepCopy(), heal))
        }
    }

    fun calculateHealRange(previews: List<PreviewComponent>): Pair<Int, Int>? {
        val healComponents = calculateHealComponents(previews)
                ?: return null
        val baseHeal = healComponents.first

        val scaleAdjustment = healComponents.second
                .filter { it is HealScaleAdjustmentPreviewComponent }
                .sumBy { (it as HealScaleAdjustmentPreviewComponent).adjustment } / 100f

        return Pair(
                baseHeal.minHeal + (baseHeal.minHeal * scaleAdjustment).toInt(),
                baseHeal.maxHeal + (baseHeal.maxHeal * scaleAdjustment).toInt())
    }

    fun calculateHealComponents(previews: List<PreviewComponent>): Pair<HealPreviewComponent, List<PreviewComponent>>? {
        val baseHeal = previews
                .find { it is HealPreviewComponent }
                ?.let { (it as HealPreviewComponent) }
                ?: return null

        val adjustments = previews.filter { it is HealScaleAdjustmentPreviewComponent }
        return Pair(baseHeal, adjustments)
    }
}
