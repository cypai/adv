package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetSkillCommand
import com.pipai.adv.backend.battle.engine.domain.*
import com.pipai.adv.backend.battle.engine.log.TargetSkillEvent

class BindAttackSkillExecutionRule : CommandExecutionRule {

    private val applicableSkills = listOf("Hamstring", "Head Strike", "Arm Strike", "Leg Strike")

    companion object {
        const val DAMAGE_RANGE = 2
    }

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is TargetSkillCommand && command.skill.schema.name in applicableSkills
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as TargetSkillCommand
        val skill = cmd.skill

        val base = when (skill.schema.name) {
            "Hamstring" -> state.npcList.getNpc(cmd.unitId)!!.unitInstance.stats.dexterity + state.getNpcWeapon(cmd.unitId)!!.schema.patk
            "Head Strike" -> state.npcList.getNpc(cmd.unitId)!!.unitInstance.stats.strength + state.getNpcWeapon(cmd.unitId)!!.schema.patk
            "Arm Strike" -> state.npcList.getNpc(cmd.unitId)!!.unitInstance.stats.strength + state.getNpcWeapon(cmd.unitId)!!.schema.patk
            "Leg Strike" -> state.npcList.getNpc(cmd.unitId)!!.unitInstance.stats.strength + state.getNpcWeapon(cmd.unitId)!!.schema.patk
            else -> throw IllegalStateException("An unexpected skill is being previewed: $skill")
        }

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        previewComponents.add(ApUsedPreviewComponent(cmd.unitId, state.apState.getNpcAp(cmd.unitId)))
        previewComponents.add(ToHitPreviewComponent(65))
        previewComponents.add(ToCritPreviewComponent(25))
        previewComponents.add(DamagePreviewComponent(base - DAMAGE_RANGE, base + DAMAGE_RANGE))
        previewComponents.add(DamageScaleAdjustmentPreviewComponent((skill.level - 1) * 5, "Skill level ${skill.level}"))

        val bodyPart = when (skill.schema.name) {
            "Hamstring" -> BodyPart.LEGS
            "Head Strike" -> BodyPart.HEAD
            "Arm Strike" -> BodyPart.ARMS
            "Leg Strike" -> BodyPart.LEGS
            else -> throw IllegalStateException("An unexpected skill is being previewed: $skill")
        }

        val bindPreviewComponents: MutableList<PreviewComponent> = mutableListOf()
        bindPreviewComponents.add(ToHitPreviewComponent(50 + 5 * (skill.level)))
        bindPreviewComponents.add(BindPreviewComponent(bodyPart, 3, 5))

        TargetStagePreviewComponent(cmd.unitId, cmd.targetId, previewComponents.toMutableList(), "Bind Stage")

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
