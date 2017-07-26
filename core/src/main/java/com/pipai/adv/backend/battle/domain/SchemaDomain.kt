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
        val mobility: Int) {

    fun toMutableUnitStats() = MutableUnitStats(hpMax, mpMax, strength, dexterity, constitution, intelligence, resistance, avoid, mobility)
}

data class MutableUnitStats(
        var hpMax: Int,
        var mpMax: Int,
        var strength: Int,
        var dexterity: Int,
        var constitution: Int,
        var intelligence: Int,
        var resistance: Int,
        var avoid: Int,
        var mobility: Int) {

    fun toUnitStats() = UnitStats(hpMax, mpMax, strength, dexterity, constitution, intelligence, resistance, avoid, mobility)
}

class UnitStatsFactory {
    var hpMax: Int = 0
    var mpMax: Int = 0
    var strength: Int = 0
    var dexterity: Int = 0
    var constitution: Int = 0
    var intelligence: Int = 0
    var resistance: Int = 0
    var avoid: Int = 0
    var mobility: Int = 0

    fun hpMax(value: Int): UnitStatsFactory {
        hpMax = value
        return this
    }

    fun mpMax(value: Int): UnitStatsFactory {
        mpMax = value
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

    fun resistance(value: Int): UnitStatsFactory {
        resistance = value
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

    fun buildUnitStats() = UnitStats(hpMax, mpMax, strength, dexterity, constitution, intelligence, resistance, avoid, mobility)

    fun buildMutableUnitStats() = MutableUnitStats(hpMax, mpMax, strength, dexterity, constitution, intelligence, resistance, avoid, mobility)
}

data class UnitSchema(
        val name: String,
        val baseStats: UnitStats)

data class UnitInstance(
        val schema: UnitSchema,
        var nickname: String,
        var hp: Int,
        var mp: Int,
        var weapon: InventoryItem.WeaponInstance?) : DeepCopyable<UnitInstance> {

    constructor(schema: UnitSchema, nickname: String) : this(schema, nickname, schema.baseStats.hpMax, schema.baseStats.mpMax, null)

    override fun deepCopy() = copy(weapon = weapon?.copy())
}

enum class WeaponType {
    BOW, SWORD
}

data class WeaponSchema(
        val name: String,
        val type: WeaponType,
        val atk: Int,
        val rarity: Int)
