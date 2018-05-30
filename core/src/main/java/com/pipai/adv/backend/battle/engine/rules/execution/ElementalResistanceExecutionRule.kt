package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.AttackElement
import com.pipai.adv.backend.battle.domain.Resistance
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.AttackElementPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.DamagePreviewComponent
import com.pipai.adv.backend.battle.engine.domain.DamageScaleAdjustmentPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent

class ElementalResistanceExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is TargetCommand
                && previews.any { it is DamagePreviewComponent }
                && previews.any { it is AttackElementPreviewComponent }
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as TargetCommand

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()

        val resistances = state.getNpc(cmd.targetId)!!.unitInstance.resistances

        val element = (previews.find { it is AttackElementPreviewComponent }!! as AttackElementPreviewComponent).element

        val elementalResistance = when (element) {
            AttackElement.FIRE -> resistances.fire
            AttackElement.ICE -> resistances.ice
            AttackElement.LIGHTNING -> resistances.lightning
        }

        when (elementalResistance) {
            Resistance.WEAK -> previewComponents.add(DamageScaleAdjustmentPreviewComponent(50, "Elemental weakness"))
            Resistance.RESIST -> previewComponents.add(DamageScaleAdjustmentPreviewComponent(-50, "Elemental resistance"))
            Resistance.NEUTRAL -> {
            }
        }

        return previewComponents
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {
    }
}
