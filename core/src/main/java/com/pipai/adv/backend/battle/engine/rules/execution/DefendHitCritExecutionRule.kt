package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.domain.*

class DefendHitCritExecutionRule : CommandExecutionRule {

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

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        if (state.checkNpcStatus(cmd.targetId, NpcStatus.DEFENDING)) {
            previewComponents.add(ToHitFlatAdjustmentPreviewComponent(-20, "Defending"))
            previewComponents.add(ToCritFlatAdjustmentPreviewComponent(-50, "Defending"))
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
