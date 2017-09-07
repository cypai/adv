package com.pipai.test.fixtures

import com.pipai.adv.SchemaList
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.adv.backend.battle.domain.UnitSchema
import com.pipai.adv.backend.battle.domain.UnitStats
import com.pipai.adv.npc.Npc

fun npcFromStats(stats: UnitStats): Npc {
    val schema = UnitSchema("Test Unit", stats)
    val unitInstance = UnitInstance(schema, "Test Unit Nickname", schema.baseStats.hpMax, schema.baseStats.mpMax, null)
    return Npc(unitInstance, EnvObjTilesetMetadata.NONE)
}

fun getSchemaList(): SchemaList {
    val schemaList = SchemaList()
    // Stats:
    // HP, MP, STR, DEX, CON, INT, RES, AVD, MOV
    schemaList.addSchema("Human", UnitStats(20, 10, 10, 10, 10, 10, 10, 0, 5),
            EnvObjTilesetMetadata.NONE)
    schemaList.addSchema("Slime", UnitStats(15, 20, 5, 5, 20, 15, 5, 0, 3),
            EnvObjTilesetMetadata.NONE)
    return schemaList
}
