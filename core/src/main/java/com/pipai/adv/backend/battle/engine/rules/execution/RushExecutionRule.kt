package com.pipai.adv.backend.battle.engine.rules.execution

import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.MoveCommand
import com.pipai.adv.backend.battle.engine.domain.*
import com.pipai.adv.backend.battle.engine.log.TextEvent
import com.pipai.adv.backend.battle.utils.BattleUtils
import com.pipai.adv.utils.MathUtils

class RushExecutionRule : CommandExecutionRule {

    override fun matches(command: BattleCommand, previews: List<PreviewComponent>): Boolean {
        return command is MoveCommand
    }

    override fun preview(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        val cmd = command as MoveCommand

        val rushSkill = state.getNpc(cmd.unitId)!!.unitInstance.skills.find { it.schema.name == "Rush" }
                ?: return listOf()

        val startPosition = cmd.path.first()
        val endPosition = cmd.path.last()
        val enemies = BattleUtils.enemiesInRange(cmd.unitId, endPosition, backend, BattleBackend.MELEE_WEAPON_DISTANCE2)

        val target = when (enemies.size) {
            0 -> null
            1 -> enemies.first()
            else -> {
                enemies.map {
                    val enemyPosition = backend.getNpcPosition(it)!!
                    Pair(it, MathUtils.distance2(startPosition.x, startPosition.y, enemyPosition.x, enemyPosition.y))
                }.minBy { it.second }!!.first
            }
        } ?: return listOf()

        val base = state.npcList.getNpc(cmd.unitId)!!.unitInstance.stats.strength + state.getNpcWeapon(cmd.unitId)!!.schema.patk

        val previewComponents: MutableList<PreviewComponent> = mutableListOf()
        previewComponents.add(ToHitPreviewComponent(65))
        previewComponents.add(ToCritPreviewComponent(0))
        previewComponents.add(DamagePreviewComponent(base - NormalAttackExecutionRule.DAMAGE_RANGE, base + NormalAttackExecutionRule.DAMAGE_RANGE))
        previewComponents.add(ToCritFlatAdjustmentPreviewComponent((rushSkill.level - 1) / 2 * 5, "Skill level ${rushSkill.level}"))
        previewComponents.add(DamageScaleAdjustmentPreviewComponent((rushSkill.level - 1) * 5, "Skill level ${rushSkill.level}"))

        return listOf(TargetStagePreviewComponent(cmd.unitId, target, previewComponents, "Rush"))
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         backend: BattleBackend,
                         state: BattleState,
                         cache: BattleBackendCache) {

        val cmd = command as MoveCommand
        state.battleLog.addEvent(TextEvent("${state.getNpc(cmd.unitId)!!.unitInstance.nickname} rushes a nearby enemy!"))
    }
}
