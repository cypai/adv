package com.pipai.adv.backend.battle.domain

import com.pipai.adv.domain.UnitSkill
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
        val baseStats: UnitStats,
        val resistances: Resistances,
        val expGiven: Int)

data class UnitInstance(
        val schema: String,
        var nickname: String,
        var level: Int,
        var exp: Int,
        var expGiven: Int,
        var stats: UnitStats,
        var hp: Int,
        var tp: Int,
        val resistances: Resistances,
        var weapon: InventoryItem.WeaponInstance?,
        val inventory: MutableList<InventorySlot>,
        val skills: MutableList<UnitSkill>) : DeepCopyable<UnitInstance> {

    companion object {
        const val DEFAULT_INVENTORY_SLOTS = 4
    }

    constructor(schema: UnitSchema, nickname: String) : this(schema.name, nickname, 1, 0, schema.expGiven, schema.baseStats.copy(),
            schema.baseStats.hpMax, schema.baseStats.tpMax, schema.resistances.copy(),
            null, generateInventorySlots(DEFAULT_INVENTORY_SLOTS), mutableListOf())

    constructor(schema: UnitSchema, nickname: String, weaponSchema: WeaponSchema)
            : this(schema.name, nickname, 1, 0, schema.expGiven, schema.baseStats.copy(),
            schema.baseStats.hpMax, schema.baseStats.tpMax, schema.resistances.copy(),
            InventoryItem.WeaponInstance(weaponSchema.name, 1), generateInventorySlots(DEFAULT_INVENTORY_SLOTS), mutableListOf())

    constructor(schema: UnitSchema, nickname: String, weaponSchema: WeaponSchema, skills: List<UnitSkill>)
            : this(schema.name, nickname, 1, 0, schema.expGiven, schema.baseStats.copy(),
            schema.baseStats.hpMax, schema.baseStats.tpMax, schema.resistances.copy(),
            InventoryItem.WeaponInstance(weaponSchema.name, 1), generateInventorySlots(DEFAULT_INVENTORY_SLOTS), skills.toMutableList())

    override fun deepCopy() = copy(weapon = weapon?.copy())
}

private fun generateInventorySlots(slots: Int): MutableList<InventorySlot> {
    val inventory: MutableList<InventorySlot> = mutableListOf()
    repeat(slots, { inventory.add(InventorySlot(null)) })
    return inventory
}

data class Resistances(var fire: Resistance,
                       var ice: Resistance,
                       var lightning: Resistance,
                       var poison: Resistance,
                       var acid: Resistance,
                       var paralyze: Resistance,
                       var blind: Resistance,
                       var head: Resistance,
                       var arm: Resistance,
                       var leg: Resistance) {
    constructor() : this(
            Resistance.NEUTRAL,
            Resistance.NEUTRAL,
            Resistance.NEUTRAL,
            Resistance.NEUTRAL,
            Resistance.NEUTRAL,
            Resistance.NEUTRAL,
            Resistance.NEUTRAL,
            Resistance.NEUTRAL,
            Resistance.NEUTRAL,
            Resistance.NEUTRAL)
}

enum class Resistance {
    WEAK, NEUTRAL, RESIST
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
        val value: Int,
        val description: String)

enum class ArmorType {
    LIGHT_ARMOR, HEAVY_ARMOR, SHIELD
}

data class ArmorSchema(
        val name: String,
        val type: ArmorType,
        val pdef: Int,
        val mdef: Int,
        val value: Int,
        val description: String)

enum class ItemType {
    CONSUMABLE, MISC
}

data class ItemSchema(
        val name: String,
        val type: ItemType,
        val value: Int,
        val description: String)
