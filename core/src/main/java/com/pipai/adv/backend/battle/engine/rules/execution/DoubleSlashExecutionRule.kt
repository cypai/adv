package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetSkillCommand
import com.pipai.adv.backend.battle.engine.domain.*
import com.pipai.adv.backend.battle.engine.log.TargetSkillEvent

class DoubleSlashExecutionRule : CommandExecutionRule {

    companion object {
        const val DAMAGE_RANGE = 2
    }

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is TargetSkillCommand && command.skill.schema.name == "Double Slash"
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as TargetSkillCommand
        val skill = cmd.skill

        val base = state.npcList.getNpc(cmd.unitId)!!.unitInstance.stats.strength + state.getNpcWeapon(cmd.unitId)!!.schema.patk

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        previewComponents.add(ToHitPreviewComponent(65))
        previewComponents.add(ToCritPreviewComponent(25))
        previewComponents.add(DamagePreviewComponent(base - DAMAGE_RANGE, base + DAMAGE_RANGE))
        previewComponents.add(DamageScaleAdjustmentPreviewComponent((skill.level - 1) * 5, "Skill level ${skill.level}"))

        return listOf(
                ApUsedPreviewComponent(cmd.unitId, state.apState.getNpcAp(cmd.unitId)),
                TargetStagePreviewComponent(cmd.unitId, cmd.targetId, previewComponents, "First Attack"),
                TargetStagePreviewComponent(cmd.unitId, cmd.targetId, previewComponents, "Second Attack"))
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
