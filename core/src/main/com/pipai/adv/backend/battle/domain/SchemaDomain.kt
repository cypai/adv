package com.pipai.adv.backend.battle.domain

import com.pipai.utils.DeepCopyable

data class UnitStats(
        val hpMax: Int,
        val mpMax: Int,
        val strength: Int,
        val dexterity: Int,
        val constitution: Int,
        val intelligence: Int,
        val resistance: Int,
        val avoid: Int,
        val mobility: Int)

data class MutableUnitStats(
        var hpMax: Int,
        var mpMax: Int,
        var strength: Int,
        var dexterity: Int,
        var constitution: Int,
        var intelligence: Int,
        var resistance: Int,
        var avoid: Int,
        var mobility: Int)

data class UnitSchema(
        val name: String,
        val baseStats: UnitStats)

data class UnitInstance(
        val schema: UnitSchema,
        var nickname: String,
        var hp: Int,
        var mp: Int,
        val stats: MutableUnitStats,
        var weapon: InventoryItem.WeaponInstance) : DeepCopyable<UnitInstance> {

    override fun deepCopy(): UnitInstance {
        return copy(stats = stats.copy(), weapon = weapon.copy())
    }
}

enum class WeaponType {
    BOW, SWORD
}

data class WeaponSchema(
        val name: String,
        val type: WeaponType,
        val atk: Int,
        val rarity: Int)
