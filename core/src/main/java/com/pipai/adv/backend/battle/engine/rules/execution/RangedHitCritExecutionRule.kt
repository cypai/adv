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
import com.pipai.adv.utils.MathUtils

class RangedHitCritExecutionRule : CommandExecutionRule {

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

        val actionCommand = command as ActionCommand

        val weapon = state.getNpcWeapon(actionCommand.unitId)!!
        val weaponSchema = backend.weaponSchemaIndex.getWeaponSchema(weapon.name)!!
        val weaponRange = weaponSchema.range

        if (command is TargetSkillCommand) {
            val skill = backend.skillIndex.getSkillSchema(command.skill.name)!!
            if (skill.rangeType == SkillRangeType.RANGED
                    || (skill.rangeType == SkillRangeType.WEAPON && weaponRange == WeaponRange.RANGED)) {
                return generateHitCritBonus(command, cache)
            } else {
                return listOf()
            }
        } else {
            if (weaponRange == WeaponRange.RANGED) {
                return generateHitCritBonus(command, cache)
            } else {
                return listOf()
            }
        }
    }

    private fun generateHitCritBonus(command: BattleCommand, cache: BattleBackendCache): List<PreviewComponent> {
        val actionCommand = command as ActionCommand
        val targetCommand = command as TargetCommand

        val attackerLocation = cache.npcPositions[actionCommand.unitId]!!
        val targetLocation = cache.npcPositions[targetCommand.targetId]!!

        val distance = MathUtils.distance(attackerLocation.x, attackerLocation.y, targetLocation.x, targetLocation.y)
        val toHitAdjustment = when {
            distance <= 3 -> (10 * (4 - distance)).toInt()
            distance <= 5 -> (5 * (6 - distance)).toInt()
            else -> 0
        }
        val toCritAdjustment = when {
            distance <= 5 -> (5 * (5 - distance)).toInt()
            else -> 0
        }
        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        if (toHitAdjustment != 0) {
            previewComponents.add(ToHitFlatAdjustmentPreviewComponent(toHitAdjustment, "Range"))
        }
        if (toCritAdjustment != 0) {
            previewComponents.add(ToCritFlatAdjustmentPreviewComponent(toCritAdjustment, "Range"))
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
