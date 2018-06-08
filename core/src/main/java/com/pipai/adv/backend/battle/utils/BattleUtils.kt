package com.pipai.adv.backend.battle.utils

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.index.WeaponSchemaIndex
import com.pipai.adv.utils.GridUtils

object BattleUtils {
    fun canTakeAction(npcId: Int, requiredAp: Int, backend: BattleBackend): Boolean {
        val npc = backend.getNpc(npcId)
        return npc != null && npc.unitInstance.hp > 0 && backend.getNpcAp(npcId) >= requiredAp
    }

    fun enemiesInWeaponRange(npcId: Int, backend: BattleBackend): List<Int> {
        val attackableEnemies: MutableList<Int> = mutableListOf()

        val attacker = backend.getBattleState().npcList.getNpc(npcId)!!
        val weapon = attacker.unitInstance.weapon

        if (weapon != null) {
            val range2 = weaponRangeDistance2(backend.weaponSchemaIndex, weapon)
            attackableEnemies.addAll(enemiesInRange(npcId, backend, range2))
        }
        return attackableEnemies
    }

    fun enemiesInWeaponRange(npcId: Int, position: GridPosition, backend: BattleBackend): List<Int> {
        val attackableEnemies: MutableList<Int> = mutableListOf()

        val attacker = backend.getBattleState().npcList.getNpc(npcId)!!
        val weapon = attacker.unitInstance.weapon

        if (weapon != null) {
            val range2 = weaponRangeDistance2(backend.weaponSchemaIndex, weapon)
            attackableEnemies.addAll(enemiesInRange(npcId, position, backend, range2))
        }
        return attackableEnemies
    }

    fun enemiesInRange(npcId: Int, backend: BattleBackend, range2: Int): List<Int> {
        val attackerPosition = backend.getNpcPosition(npcId)!!
        return enemiesInRange(npcId, attackerPosition, backend, range2)
    }

    fun enemiesInRange(npcId: Int, position: GridPosition, backend: BattleBackend, range2: Int): List<Int> {
        val enemiesInRange: MutableList<Int> = mutableListOf()
        val enemyTeam = Team.opposingTeam(backend.getNpcTeam(npcId)!!)
        val potentialTargetIds = backend.getTeam(enemyTeam)

        potentialTargetIds.forEach {
            val targetPosition = backend.getNpcPosition(it)!!
            val distance2 = GridUtils.gridDistance2(position, targetPosition)
            if (distance2 < range2) {
                enemiesInRange.add(it)
            }
        }
        return enemiesInRange
    }

    fun teammatesInRange(npcId: Int, backend: BattleBackend, range2: Int): List<Int> {
        val position = backend.getNpcPosition(npcId)!!
        return teammatesInRange(npcId, position, backend, range2)
    }

    fun teammatesInRange(npcId: Int, position: GridPosition, backend: BattleBackend, maxRange2: Int): List<Int> {
        val teammatesInRange: MutableList<Int> = mutableListOf()
        val potentialTeammateIds = backend.getTeam(backend.getNpcTeam(npcId)!!)

        potentialTeammateIds.forEach {
            val teammatePosition = backend.getNpcPosition(it)!!
            val distance2 = GridUtils.gridDistance2(position, teammatePosition)
            if (distance2 < maxRange2) {
                teammatesInRange.add(it)
            }
        }
        return teammatesInRange
    }

    fun weaponRangeDistance2(weaponSchemaIndex: WeaponSchemaIndex, weapon: InventoryItem.WeaponInstance): Int {
        val schema = weaponSchemaIndex.getWeaponSchema(weapon.name)!!
        return when (schema.range) {
            WeaponRange.RANGED -> BattleBackend.RANGED_WEAPON_DISTANCE2
            WeaponRange.MELEE -> BattleBackend.MELEE_WEAPON_DISTANCE2
        }
    }

    fun weaponCanAttack(weaponSchemaIndex: WeaponSchemaIndex, weapon: InventoryItem.WeaponInstance, ammoRequired: Int): Boolean {
        return when (weaponRequiresAmmo(weaponSchemaIndex, weapon)) {
            true -> weapon.ammo >= ammoRequired
            false -> true
        }
    }

    fun weaponRequiresAmmo(weaponSchemaIndex: WeaponSchemaIndex, weapon: InventoryItem.WeaponInstance): Boolean {
        val schema = weaponSchemaIndex.getWeaponSchema(weapon.name)!!
        return schema.attributes.contains(WeaponAttribute.CAN_FAST_RELOAD)
                || schema.attributes.contains(WeaponAttribute.CAN_RELOAD)
    }
}
