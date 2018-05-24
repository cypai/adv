package com.pipai.adv.backend.battle.engine.rules.command

import com.pipai.adv.backend.battle.domain.WeaponRange
import com.pipai.adv.backend.battle.domain.WeaponType
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetSkillCommand
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus
import com.pipai.adv.classes.skills.DoubleSlash
import com.pipai.adv.utils.MathUtils

class DoubleSlashSanityRule : CommandRule {

    private val usableWeaponTypes = listOf(WeaponType.SWORD, WeaponType.DAGGER)

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is TargetSkillCommand && command.skill is DoubleSlash) {
            val weapon = state.getNpcWeapon(command.unitId)
            weapon ?: return ExecutableStatus(false, "Attacker is not wielding a weapon")

            if (!usableWeaponTypes.contains(weapon.schema.type)) {
                return ExecutableStatus(false, "Attacker is not wielding a weapon suitable for this skill")
            }

            val range = weapon.schema.range
            val attackerPosition = cache.npcPositions[command.unitId]!!
            val targetPosition = cache.npcPositions[command.targetId]!!

            val distanceLimit = when (range) {
                WeaponRange.MELEE -> BattleBackend.MELEE_WEAPON_DISTANCE2
                WeaponRange.RANGED -> BattleBackend.RANGED_WEAPON_DISTANCE2
            }
            val distance2 = MathUtils.distance2(attackerPosition.x, attackerPosition.y, targetPosition.x, targetPosition.y)
            if (distance2 >= distanceLimit) {
                return ExecutableStatus(false, "Attacking distance is too great")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }
}