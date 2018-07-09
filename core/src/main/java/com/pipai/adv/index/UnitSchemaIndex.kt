package com.pipai.adv.index

import com.pipai.adv.SchemaMetadata
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.domain.Resistances
import com.pipai.adv.backend.battle.domain.UnitSchema
import com.pipai.adv.backend.battle.domain.UnitStats

class UnitSchemaIndex : Iterable<SchemaMetadata> {
    private val schemas: MutableMap<String, SchemaMetadata> = mutableMapOf()

    override operator fun iterator(): Iterator<SchemaMetadata> {
        return schemas.values.iterator()
    }

    fun addSchema(name: String, stats: UnitStats, resistances: Resistances, expGiven: Int, tilesetMetadata: EnvObjTilesetMetadata) {
        schemas.put(name, SchemaMetadata(UnitSchema(name, stats, resistances, expGiven), tilesetMetadata))
    }

    fun getSchema(name: String): SchemaMetadata {
        if (schemas.contains(name)) {
            return schemas.get(name)!!
        } else {
            throw IllegalArgumentException("$name does not exist in name list")
        }
    }
}