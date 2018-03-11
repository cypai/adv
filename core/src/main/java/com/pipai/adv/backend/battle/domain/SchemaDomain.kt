package com.pipai.adv.backend.battle.domain

import com.pipai.adv.utils.DeepCopyable

data class UnitStats(
        val hpMax: Int,
        val tpMax: Int,
        val strength: Int,
        val dexterity: Int,
        val constitution: Int,
        val intelligence: Int,
        val wisdom: Int,
        val avoid: Int,
        val mobility: Int) {

    fun toMutableUnitStats() = MutableUnitStats(hpMax, tpMax, strength, dexterity, constitution, intelligence, wisdom, avoid, mobility)
}

data class MutableUnitStats(
        var hpMax: Int,
        var tpMax: Int,
        var strength: Int,
        var dexterity: Int,
        var constitution: Int,
        var intelligence: Int,
        var wisdom: Int,
        var avoid: Int,
        var mobility: Int) {

    fun toUnitStats() = UnitStats(hpMax, tpMax, strength, dexterity, constitution, intelligence, wisdom, avoid, mobility)
}

class UnitStatsFactory {
    var hpMax: Int = 0
    var tpMax: Int = 0
    var strength: Int = 0
    var dexterity: Int = 0
    var constitution: Int = 0
    var intelligence: Int = 0
    var wisdom: Int = 0
    var avoid: Int = 0
    var mobility: Int = 0

    fun hpMax(value: Int): UnitStatsFactory {
        hpMax = value
        return this
    }

    fun tpMax(value: Int): UnitStatsFactory {
        tpMax = value
        return this
    }

    fun strength(value: Int): UnitStatsFactory {
        strength = value
        return this
    }

    fun dexterity(value: Int): UnitStatsFactory {
        dexterity = value
        return this
    }

    fun constitution(value: Int): UnitStatsFactory {
        constitution = value
        return this
    }

    fun intelligence(value: Int): UnitStatsFactory {
        intelligence = value
        return this
    }

    fun wisdom(value: Int): UnitStatsFactory {
        wisdom = value
        return this
    }

    fun avoid(value: Int): UnitStatsFactory {
        avoid = value
        return this
    }

    fun mobility(value: Int): UnitStatsFactory {
        mobility = value
        return this
    }

    fun buildUnitStats() = UnitStats(hpMax, tpMax, strength, dexterity, constitution, intelligence, wisdom, avoid, mobility)

    fun buildMutableUnitStats() = MutableUnitStats(hpMax, tpMax, strength, dexterity, constitution, intelligence, wisdom, avoid, mobility)
}

data class UnitSchema(
        val name: String,
        val baseStats: UnitStats)

data class UnitInstance(
        val schema: UnitSchema,
        var nickname: String,
        var hp: Int,
        var tp: Int,
        var weapon: InventoryItem.WeaponInstance?) : DeepCopyable<UnitInstance> {

    constructor(schema: UnitSchema, nickname: String) : this(schema, nickname, schema.baseStats.hpMax, schema.baseStats.tpMax, null)

    constructor(schema: UnitSchema, nickname: String, weaponSchema: WeaponSchema)
            : this(schema, nickname, schema.baseStats.hpMax, schema.baseStats.tpMax, InventoryItem.WeaponInstance(weaponSchema, 1))

    override fun deepCopy() = copy(weapon = weapon?.copy())
}

enum class WeaponType {
    SWORD, SPEAR, DAGGER, BOW, RIFLE, PISTOL, SHOTGUN, STAFF, SHURIKEN, MONSTER
}

enum class WeaponRange {
    MELEE, RANGED
}

enum class WeaponAttribute {
    CAN_FAST_RELOAD, CAN_RELOAD, CAN_STEADY
}

data class WeaponSchema(
        val name: String,
        val type: WeaponType,
        val range: WeaponRange,
        val patk: Int,
        val matk: Int,
        val attributes: List<WeaponAttribute>,
        val magazineSize: Int,
        val description: String)
