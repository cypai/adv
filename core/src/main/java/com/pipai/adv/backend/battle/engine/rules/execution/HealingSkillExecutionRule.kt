package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetSkillCommand
import com.pipai.adv.backend.battle.engine.domain.ApUsedPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.HealPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.HealScaleAdjustmentPreviewComponent
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.TargetSkillEvent

class HealingSkillExecutionRule : CommandExecutionRule {

    private val applicableSkills = listOf("Heal", "Ki")

    companion object {
        const val HEAL_RANGE = 2
    }

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is TargetSkillCommand && command.skill.name in applicableSkills
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as TargetSkillCommand
        val skill = cmd.skill

        val base = state.npcList.getNpc(cmd.unitId)!!.unitInstance.stats.wisdom

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        previewComponents.add(ApUsedPreviewComponent(cmd.unitId, state.apState.getNpcAp(cmd.unitId)))
        previewComponents.add(HealPreviewComponent(base - HEAL_RANGE, base + HEAL_RANGE))
        previewComponents.add(HealScaleAdjustmentPreviewComponent(skill.level * 10, "Skill level ${skill.level}"))

        return previewComponents
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {

        val cmd = command as TargetSkillCommand
        state.battleLog.addEvent(TargetSkillEvent(
                cmd.unitId,
                state.npcList.getNpc(cmd.unitId)!!,
                cmd.targetId,
                state.npcList.getNpc(cmd.targetId)!!,
                cmd.skill))
    }

}
