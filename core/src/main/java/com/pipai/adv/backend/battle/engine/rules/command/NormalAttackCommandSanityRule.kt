package com.pipai.adv.backend.battle.engine.rules.command

import com.pipai.adv.backend.battle.domain.WeaponAttribute
import com.pipai.adv.backend.battle.domain.WeaponRange
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.BattleBackendCache
import com.pipai.adv.backend.battle.engine.BattleState
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommand
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus
import com.pipai.adv.utils.MathUtils

class NormalAttackCommandSanityRule : CommandRule {

    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is NormalAttackCommand) {
            val attacker = state.npcList.getNpc(command.unitId)!!
            val weapon = command.weapon
            if (attacker.unitInstance.weapon != weapon) {
                return ExecutableStatus(false, "Attacker is not wielding this weapon")
            }

            val range = command.weapon.schema.range
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

            if (weapon.ammo <= 0
                    && (weapon.schema.attributes.contains(WeaponAttribute.CAN_RELOAD)
                            || weapon.schema.attributes.contains(WeaponAttribute.CAN_FAST_RELOAD))) {

                return ExecutableStatus(false, "This weapon needs to be reloaded")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }
}