package com.pipai.adv

import com.badlogic.gdx.Gdx
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata
import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.adv.backend.battle.domain.UnitSchema
import com.pipai.adv.backend.battle.domain.UnitStats
import com.pipai.adv.npc.Npc
import com.pipai.adv.save.AdvSave
import com.pipai.adv.tiles.GrassyTileset
import com.pipai.adv.tiles.MapTileset
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.tiles.TextureManager

data class AdvGameGlobals(val save: AdvSave,
                          val schemaList: SchemaList,
                          val mapTilesetList: MapTilesetList,
                          val textureManager: TextureManager,
                          val pccManager: PccManager)

class AdvGameInitializer() {
    fun initializeGlobals(): AdvGameGlobals {
        val schemaList = initializeSchemaList()
        val save = generateNewSave(schemaList)
        val tilesetList = initializeMapTilesetList()
        val textureManager = TextureManager()
        textureManager.loadAllTextures()
        val pccManager = initializePccManager(save, schemaList)
        return AdvGameGlobals(save, schemaList, tilesetList, textureManager, pccManager)
    }

    private fun generateNewSave(schemas: SchemaList): AdvSave {
        val save = AdvSave()

        val playerPcc: MutableList<PccMetadata> = mutableListOf()
        playerPcc.add(PccMetadata("body", 2))
        val playerNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, "Amber"),
                PccTilesetMetadata(playerPcc))
        save.globalNpcList.addNpc(playerNpc)

        val friendPcc: MutableList<PccMetadata> = mutableListOf()
        friendPcc.add(PccMetadata("body", 1))
        friendPcc.add(PccMetadata("eye", 7))
        friendPcc.add(PccMetadata("hair", 0))
        friendPcc.add(PccMetadata("pants", 13))
        friendPcc.add(PccMetadata("cloth", 63))
        friendPcc.add(PccMetadata("etc", 205))
        val friendNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, "Len"),
                PccTilesetMetadata(friendPcc))
        save.globalNpcList.addNpc(friendNpc)

        return save
    }

    private fun initializeSchemaList(): SchemaList {
        val schemaList = SchemaList()
        // Stats:
        // HP, MP, STR, DEX, CON, INT, RES, AVD, MOV
        schemaList.addSchema("Human", UnitStats(20, 10, 10, 10, 10, 10, 10, 0, 5),
                EnvObjTilesetMetadata.NONE)
        schemaList.addSchema("Slime", UnitStats(15, 20, 5, 5, 20, 15, 5, 0, 3),
                EnvObjTilesetMetadata.NONE)
        return schemaList
    }

    private fun initializeMapTilesetList(): MapTilesetList {
        val tilesetList = MapTilesetList()

        tilesetList.addTileset("grassy",
                GrassyTileset(Gdx.files.internal("assets/binassets/graphics/tilesets/outside_tileset.png")))

        return tilesetList
    }

    private fun initializePccManager(save: AdvSave, schemaList: SchemaList): PccManager {
        val pccManager = PccManager()
        val pccMetadataList: MutableList<PccMetadata> = mutableListOf()

        save.globalNpcList.map { it.value.tilesetMetadata }
                .filter { it is PccTilesetMetadata }
                .forEach { pccMetadataList.addAll((it as PccTilesetMetadata).pccMetadata) }

        schemaList.map { it.tilesetMetadata }
                .filter { it is PccTilesetMetadata }
                .forEach { pccMetadataList.addAll((it as PccTilesetMetadata).pccMetadata) }

        pccManager.loadPccTextures(pccMetadataList)

        return pccManager
    }
}

class SchemaList : Iterable<SchemaMetadata> {
    private val schemas: MutableMap<String, SchemaMetadata> = mutableMapOf()

    override operator fun iterator(): Iterator<SchemaMetadata> {
        return schemas.values.iterator()
    }

    fun addSchema(name: String, stats: UnitStats, tilesetMetadata: EnvObjTilesetMetadata) {
        schemas.put(name, SchemaMetadata(UnitSchema(name, stats), tilesetMetadata))
    }

    fun getSchema(name: String): SchemaMetadata {
        if (schemas.contains(name)) {
            return schemas.get(name)!!
        } else {
            throw IllegalArgumentException("$name does not exist in schema list")
        }
    }
}

data class SchemaMetadata(val schema: UnitSchema, val tilesetMetadata: EnvObjTilesetMetadata)

class MapTilesetList {
    private val tilesets: MutableMap<String, MapTileset> = mutableMapOf()

    fun addTileset(name: String, tileset: MapTileset) {
        tilesets.put(name, tileset)
    }

    fun getTileset(name: String): MapTileset {
        if (tilesets.contains(name)) {
            return tilesets.get(name)!!
        } else {
            throw IllegalArgumentException("$name does not exist in tilesets list")
        }
    }
}
