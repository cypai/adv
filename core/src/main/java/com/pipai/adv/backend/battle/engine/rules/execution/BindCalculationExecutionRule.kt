package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.calculators.BindCalculator
import com.pipai.adv.backend.battle.engine.calculators.HitCalculator
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.BindPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToHitPreviewComponent
import com.pipai.adv.backend.battle.engine.log.BindEvent
import com.pipai.adv.utils.RNG

/**
 * This execution rule should be after everything else is evaluated to calculate damage correctly.
 */
class BindCalculationExecutionRule(private val hitCalculator: HitCalculator,
                                   private val bindCalculator: BindCalculator) : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is TargetCommand
                && previews.any { it is ToHitPreviewComponent }
                && previews.any { it is BindPreviewComponent }
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
        val toHit = hitCalculator.calculateToHit(previews)
                ?: throw IllegalStateException("To hit unavailable")

        val hitRoll = RNG.nextInt(100)
        if (hitRoll < toHit) {
            val bindComponent = bindCalculator.bindComponents(previews)!!
            val bindTurnRange = bindCalculator.calculateBindTurnRange(previews)
                    ?: throw IllegalStateException("Bind turn range unavailable")
            val turns = RNG.nextInt(bindTurnRange.second - bindTurnRange.first + 1) + bindTurnRange.first

            val currentTurns = state.npcStatusState.getNpcBind(cmd.targetId, bindComponent.bodyPart)
            state.npcStatusState.setNpcBind(cmd.targetId, bindComponent.bodyPart, currentTurns + turns)

            val target = state.npcList.get(cmd.targetId)!!
            state.battleLog.addEvent(BindEvent(cmd.targetId, target, bindComponent.bodyPart, currentTurns))
        }
    }

}
