package com.pipai.adv

import com.badlogic.gdx.Gdx
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata
import com.pipai.adv.backend.battle.domain.UnitSchema
import com.pipai.adv.backend.battle.domain.UnitStats
import com.pipai.adv.save.AdvSave
import com.pipai.adv.save.SaveManager
import com.pipai.adv.tiles.*

data class AdvGameGlobals(var save: AdvSave?,
                          val saveManager: SaveManager,
                          val schemaList: SchemaList,
                          val mapTilesetList: MapTilesetList,
                          val textureManager: TextureManager,
                          val pccManager: PccManager,
                          val animatedTilesetManager: AnimatedTilesetManager) {

    fun writeSave(slot: Int) {
        saveManager.save(slot, save!!)
    }

    fun load(slot: Int) {
        val loadedSave = saveManager.load(slot)
        loadSave(loadedSave)
    }

    fun loadSave(save: AdvSave) {
        this.save = save

        val pccMetadataList: MutableList<PccMetadata> = mutableListOf()

        save.globalNpcList.map { it.value.tilesetMetadata }
                .filter { it is PccTilesetMetadata }
                .forEach { pccMetadataList.addAll((it as PccTilesetMetadata).pccMetadata) }

        schemaList.map { it.tilesetMetadata }
                .filter { it is PccTilesetMetadata }
                .forEach { pccMetadataList.addAll((it as PccTilesetMetadata).pccMetadata) }

        pccManager.loadPccTextures(pccMetadataList)
    }
}

class AdvGameInitializer() {
    fun initializeGlobals(): AdvGameGlobals {
        val schemaList = initializeSchemaList()
        val tilesetList = initializeMapTilesetList()
        val textureManager = TextureManager()
        textureManager.loadAllTextures()
        val pccManager = PccManager()
        val animatedTilesetManager = AnimatedTilesetManager()
        val animatedUnitTilesets = schemaList.filter { it.tilesetMetadata is EnvObjTilesetMetadata.AnimatedUnitTilesetMetadata }
                .map { (it.tilesetMetadata as EnvObjTilesetMetadata.AnimatedUnitTilesetMetadata).filename }
        animatedTilesetManager.loadTextures(animatedUnitTilesets)
        return AdvGameGlobals(null, SaveManager(), schemaList, tilesetList, textureManager, pccManager, animatedTilesetManager)
    }

    private fun initializeSchemaList(): SchemaList {
        val schemaList = SchemaList()
        // Stats:
        // HP, MP, STR, DEX, CON, INT, RES, AVD, MOV
        schemaList.addSchema("Human", UnitStats(20, 10, 10, 10, 10, 10, 10, 0, 10),
                EnvObjTilesetMetadata.NONE)
        schemaList.addSchema("Slime", UnitStats(15, 20, 5, 5, 20, 15, 5, 0, 7),
                EnvObjTilesetMetadata.AnimatedUnitTilesetMetadata("slime.png"))
        return schemaList
    }

    private fun initializeMapTilesetList(): MapTilesetList {
        val tilesetList = MapTilesetList()

        tilesetList.addTileset("grassy",
                GrassyTileset(Gdx.files.internal("assets/binassets/graphics/tilesets/outside_tileset.png")))

        return tilesetList
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
