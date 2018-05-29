package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.calculators.CoverCalculator
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.*
import com.pipai.adv.utils.GridUtils

class CoverHitCritExecutionRule(private val coverCalculator: CoverCalculator) : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is TargetCommand
                && previews.any { it is ToHitPreviewComponent }
                && previews.any { it is ToCritPreviewComponent }
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as TargetCommand

        val attackerPosition = cache.getNpcPosition(cmd.unitId)!!
        val defenderPosition = cache.getNpcPosition(cmd.targetId)!!

        if (GridUtils.isNeighbor(attackerPosition, defenderPosition)) {
            return emptyList()
        }

        val bestCover = coverCalculator.bestCoverAgainstAttack(defenderPosition, attackerPosition)

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        when (bestCover) {
            CoverType.HALF -> {
                previewComponents.add(ToHitFlatAdjustmentPreviewComponent(-20, "Half Cover"))
                previewComponents.add(ToCritFlatAdjustmentPreviewComponent(-20, "Half Cover"))
            }
            CoverType.FULL -> {
                previewComponents.add(ToHitFlatAdjustmentPreviewComponent(-40, "Full Cover"))
                previewComponents.add(ToCritFlatAdjustmentPreviewComponent(-40, "Full Cover"))
            }
            else -> {
            }
        }

        return previewComponents.toList()
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {
    }
}
