package com.pipai.adv.backend.battle.utils

import com.pipai.adv.backend.battle.domain.InventoryItem
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.domain.WeaponAttribute
import com.pipai.adv.backend.battle.domain.WeaponRange
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.utils.GridUtils

object BattleUtils {
    fun canTakeAction(npcId: Int, requiredAp: Int, backend: BattleBackend): Boolean {
        val npc = backend.getNpc(npcId)
        return npc != null && npc.unitInstance.hp > 0 && backend.getNpcAp(npcId) >= requiredAp
    }

    fun enemiesInRange(npcId: Int, backend: BattleBackend): List<Int> {
        val attackableEnemies: MutableList<Int> = mutableListOf()

        val attacker = backend.getBattleState().npcList.getNpc(npcId)!!
        val weapon = attacker.unitInstance.weapon

        if (weapon != null) {
            val attackerPosition = backend.getNpcPosition(npcId)!!
            val enemyTeam = Team.opposingTeam(backend.getNpcTeam(npcId)!!)
            val potentialTargetIds = backend.getTeam(enemyTeam)

            val weaponDistance2 = BattleUtils.weaponRangeDistance2(weapon)
            potentialTargetIds.forEach {
                val targetPosition = backend.getNpcPosition(it)!!
                val distance2 = GridUtils.gridDistance2(attackerPosition, targetPosition)
                if (distance2 < weaponDistance2) {
                    attackableEnemies.add(it)
                }
            }
        }
        return attackableEnemies
    }

    fun weaponRangeDistance2(weapon: InventoryItem.WeaponInstance): Int {
        return when (weapon.schema.range) {
            WeaponRange.RANGED -> BattleBackend.RANGED_WEAPON_DISTANCE2
            WeaponRange.MELEE -> BattleBackend.MELEE_WEAPON_DISTANCE2
        }
    }

    fun weaponCanAttack(weapon: InventoryItem.WeaponInstance, ammoRequired: Int): Boolean {
        return when (weaponRequiresAmmo(weapon)) {
            true -> weapon.ammo >= ammoRequired
            false -> true
        }
    }

    fun weaponRequiresAmmo(weapon: InventoryItem.WeaponInstance): Boolean {
        return weapon.schema.attributes.contains(WeaponAttribute.CAN_FAST_RELOAD)
                || weapon.schema.attributes.contains(WeaponAttribute.CAN_RELOAD)
    }
}
