package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.domain.WeaponRange
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetCommand
import com.pipai.adv.backend.battle.engine.commands.TargetSkillCommand
import com.pipai.adv.backend.battle.engine.domain.*
import com.pipai.adv.domain.SkillRangeType

class MeleeHitCritExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is ActionCommand
                && command is TargetCommand
                && previews.any { it is ToHitPreviewComponent }
                && previews.any { it is ToCritPreviewComponent }
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as ActionCommand

        val weapon = state.getNpcWeapon(cmd.unitId)!!
        val weaponSchema = backend.weaponSchemaIndex.getWeaponSchema(weapon.name)!!
        val weaponRange = weaponSchema.range

        if (cmd is TargetSkillCommand) {
            val skill = backend.skillIndex.getSkillSchema(cmd.skill.name)!!
            if (skill.rangeType == SkillRangeType.MELEE
                    || (skill.rangeType == SkillRangeType.WEAPON && weaponRange == WeaponRange.MELEE)) {
                return generateHitCritBonus()
            } else {
                return listOf()
            }
        } else {
            if (weaponRange == WeaponRange.MELEE) {
                return generateHitCritBonus()
            } else {
                return listOf()
            }
        }
    }

    private fun generateHitCritBonus(): List<PreviewComponent> {
        return listOf(
                ToHitFlatAdjustmentPreviewComponent(30, "Melee weapon"),
                ToCritFlatAdjustmentPreviewComponent(25, "Melee weapon"))
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {
    }
}
