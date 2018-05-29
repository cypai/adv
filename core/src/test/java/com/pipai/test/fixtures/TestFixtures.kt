package com.pipai.test.fixtures

import com.pipai.adv.SchemaList
import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.domain.Npc

fun npcFromStats(stats: UnitStats, weapon: InventoryItem.WeaponInstance?): Npc {
    val schema = UnitSchema("Test Unit", stats, Resistances(), 0)
    val unitInstance = UnitInstance(schema, "Test Unit Nickname", 1, 0, 0, schema.baseStats.copy(),
            schema.baseStats.hpMax, schema.baseStats.tpMax, Resistances(), weapon, mutableListOf())
    return Npc(unitInstance, EnvObjTilesetMetadata.NONE)
}

fun swordFixture(): InventoryItem.WeaponInstance {
    val schema = WeaponSchema("Sword", WeaponType.SWORD, WeaponRange.MELEE, 1, 1, listOf(), 1, "")
    return InventoryItem.WeaponInstance(schema, 1)
}

fun bowFixture(): InventoryItem.WeaponInstance {
    val schema = WeaponSchema("Bow", WeaponType.BOW, WeaponRange.RANGED, 1, 1, listOf(WeaponAttribute.CAN_FAST_RELOAD), 3, "")
    return InventoryItem.WeaponInstance(schema, 3)
}

fun getSchemaList(): SchemaList {
    val schemaList = SchemaList()
    // Stats:
    // HP, MP, STR, DEX, CON, INT, RES, AVD, MOV
    schemaList.addSchema("Human", UnitStats(20, 10, 10, 10, 10, 10, 10, 0, 5), Resistances(), 0,
            EnvObjTilesetMetadata.NONE)
    schemaList.addSchema("Slime", UnitStats(15, 20, 5, 5, 20, 15, 5, 0, 3), Resistances(), 0,
            EnvObjTilesetMetadata.NONE)
    return schemaList
}
