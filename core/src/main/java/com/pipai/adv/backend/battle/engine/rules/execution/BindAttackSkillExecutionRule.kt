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
        return command is TargetSkillCommand && command.skill.name in applicableSkills
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as TargetSkillCommand
        val skill = cmd.skill
        val weapon = state.getNpcWeapon(cmd.unitId)!!
        val weaponSchema = backend.weaponSchemaIndex.getWeaponSchema(weapon.name)!!

        val base = when (skill.name) {
            "Hamstring" -> state.npcList.getNpc(cmd.unitId)!!.unitInstance.stats.dexterity + weaponSchema.patk
            "Head Strike" -> state.npcList.getNpc(cmd.unitId)!!.unitInstance.stats.strength + weaponSchema.patk
            "Arm Strike" -> state.npcList.getNpc(cmd.unitId)!!.unitInstance.stats.strength + weaponSchema.patk
            "Leg Strike" -> state.npcList.getNpc(cmd.unitId)!!.unitInstance.stats.strength + weaponSchema.patk
            else -> throw IllegalStateException("An unexpected skill is being previewed: $skill")
        }

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        previewComponents.add(ApUsedPreviewComponent(cmd.unitId, state.apState.getNpcAp(cmd.unitId)))
        previewComponents.add(ToHitPreviewComponent(65))
        previewComponents.add(ToCritPreviewComponent(25))
        previewComponents.add(DamagePreviewComponent(base - DAMAGE_RANGE, base + DAMAGE_RANGE))
        previewComponents.add(DamageScaleAdjustmentPreviewComponent((skill.level - 1) * 5, "Skill level ${skill.level}"))

        val bodyPart = when (skill.name) {
            "Hamstring" -> BodyPart.LEGS
            "Head Strike" -> BodyPart.HEAD
            "Arm Strike" -> BodyPart.ARMS
            "Leg Strike" -> BodyPart.LEGS
            else -> throw IllegalStateException("An unexpected skill is being previewed: $skill")
        }

        val bindPreviewComponents: MutableList<PreviewComponent> = mutableListOf()
        val bindChance = 50 + 5 * (skill.level)
        bindPreviewComponents.add(ToHitPreviewComponent(bindChance))
        bindPreviewComponents.add(BindPreviewComponent(bodyPart, 3, 5))

        previewComponents.add(TargetStagePreviewComponent(
                cmd.unitId, cmd.targetId, bindPreviewComponents,
                StageTypeDescription(StageType.EFFECT, "Bind ${bodyPart.niceString}", "$bindChance %")))

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
