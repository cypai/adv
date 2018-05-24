package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToCritPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToHitFlatAdjustmentPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.ToHitPreviewComponent

class AvoidHitCritExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is TargetCommand
                && previews.any { it is ToHitPreviewComponent }
                && previews.any { it is ToCritPreviewComponent }
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as TargetCommand
        val targetId = cmd.targetId
        val avoid = state.getNpc(targetId)!!.unitInstance.schema.baseStats.avoid

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        if (avoid != 0) {
            previewComponents.add(ToHitFlatAdjustmentPreviewComponent(-avoid, "Avoid"))
        }

        return previewComponents.toList()
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache) {
    }
}
