package com.pipai.test.fixtures

import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.adv.backend.battle.domain.UnitSchema
import com.pipai.adv.backend.battle.domain.UnitStats
import com.pipai.adv.npc.EnvObjTilesetMetadata
import com.pipai.adv.npc.Npc
import com.pipai.adv.tiles.EnvObjTilesetType

fun npcFromStats(stats: UnitStats): Npc {
    val schema = UnitSchema("Test Unit", stats)
    val unitInstance = UnitInstance(schema, "Test Unit Nickname", schema.baseStats.hpMax, schema.baseStats.mpMax, null)
    return Npc(unitInstance, EnvObjTilesetMetadata(EnvObjTilesetType.NONE, null, null))
}
