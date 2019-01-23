package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.AttackElement
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetSkillCommand
import com.pipai.adv.backend.battle.engine.domain.*
import com.pipai.adv.backend.battle.engine.log.TargetSkillEvent

class ElementalSkillExecutionRule : CommandExecutionRule {

    private val applicableSkills = listOf("Fireball", "Icicle", "Thunder")

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

        val base = state.npcList.get(cmd.unitId)!!.unitInstance.stats.intelligence + weaponSchema.patk

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        previewComponents.add(ApUsedPreviewComponent(cmd.unitId, state.apState.getNpcAp(cmd.unitId)))
        previewComponents.add(ToHitPreviewComponent(65))
        previewComponents.add(ToCritPreviewComponent(25))
        previewComponents.add(DamagePreviewComponent(base - DAMAGE_RANGE, base + DAMAGE_RANGE))
        previewComponents.add(DamageScaleAdjustmentPreviewComponent((skill.level - 1) * 5, "Skill level ${skill.level}"))
        val elementPreviewComponent = when (skill.name) {
            "Fireball" -> AttackElementPreviewComponent(AttackElement.FIRE)
            "Icicle" -> AttackElementPreviewComponent(AttackElement.ICE)
            "Thunder" -> AttackElementPreviewComponent(AttackElement.LIGHTNING)
            else -> throw IllegalStateException("An unexpected skill is being previewed: $skill")
        }
        previewComponents.add(elementPreviewComponent)

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
                state.npcList.get(cmd.unitId)!!,
                cmd.targetId,
                state.npcList.get(cmd.targetId)!!,
                cmd.skill))
    }

}
