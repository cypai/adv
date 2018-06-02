package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.SkillTpCheckCommand
import com.pipai.adv.backend.battle.engine.commands.TargetSkillCommand
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.domain.TpUsedPreviewComponent

class SkillTpUseExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is SkillTpCheckCommand || command is TargetSkillCommand
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val unitId = (command as ActionCommand).unitId
        val skill = when (command) {
            is SkillTpCheckCommand -> {
                command.skill
            }
            is TargetSkillCommand -> {
                command.skill
            }
            else -> throw IllegalStateException("Unexpected command $command received")
        }
        val tpRequired = when (skill.schema.name) {
            "Double Slash" -> 5
            else -> 0
        }
        return listOf(TpUsedPreviewComponent(unitId, tpRequired))
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {

    }

}
