package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetStageExecuteCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.domain.TargetStagePreviewComponent

/**
 * This execution rule should be after everything else is evaluated to calculate damage correctly.
 */
class StagePreviewExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return previews.any { it is TargetStagePreviewComponent }
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        previews.filter { it is TargetStagePreviewComponent }
                .map { it as TargetStagePreviewComponent }
                .forEach {
                    val newPreviews = backend.preview(TargetStageExecuteCommand(it))
                    it.previews.clear()
                    it.previews.addAll(newPreviews)
                }
        return listOf()
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {

        previews.filter { it is TargetStagePreviewComponent }
                .map { it as TargetStagePreviewComponent }
                .forEach { backend.executeStagePreview(TargetStageExecuteCommand(it)) }
    }
}
