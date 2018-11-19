package com.pipai.adv

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata
import com.pipai.adv.backend.progression.ProgressionBackend
import com.pipai.adv.index.*
import com.pipai.adv.map.WorldMap
import com.pipai.adv.map.WorldMapLocation
import com.pipai.adv.save.AdvSave
import com.pipai.adv.save.SaveManager
import com.pipai.adv.tiles.*
import com.pipai.adv.utils.getLogger

data class AdvGameGlobals(var save: AdvSave?,
                          val progressionBackend: ProgressionBackend,
                          val saveManager: SaveManager,
                          val unitSchemaIndex: UnitSchemaIndex,
                          val weaponSchemaIndex: WeaponSchemaIndex,
                          val armorSchemaIndex: ArmorSchemaIndex,
                          val itemSchemaIndex: ItemSchemaIndex,
                          val skillIndex: SkillIndex,
                          val mapTilesetList: MapTilesetList,
                          val textureManager: TextureManager,
                          val pccManager: PccManager,
                          val animatedTilesetManager: AnimatedTilesetManager,
                          val worldMap: WorldMap) {

    private val logger = getLogger()

    val shaderProgram: ShaderProgram

    init {
        val vertexShader = Gdx.files.local("assets/shaders/vertex.glsl").readString()
        val fragmentShader = Gdx.files.local("assets/shaders/fragment.glsl").readString()
        shaderProgram = ShaderProgram(vertexShader, fragmentShader)
        shaderProgram.setAttributef("a_color_inter1", 0f, 0f, 0f, 0f)
        shaderProgram.setAttributef("a_color_inter2", 0f, 0f, 0f, 0f)
        if (!shaderProgram.isCompiled) {
            throw RuntimeException("Could not compile shader: ${shaderProgram.log}")
        }
    }

    fun writeSave(slot: Int) {
        saveManager.save(slot, save!!)
        logger.debug("Save written to slot $slot")
    }

    fun autoSave() {
        saveManager.save(0, save!!)
        logger.debug("Autosave written")
    }

    fun loadSave(slot: Int) {
        val loadedSave = saveManager.load(slot)
        loadSave(loadedSave)
    }

    fun loadSave(save: AdvSave) {
        this.save = save

        val pccMetadataList: MutableList<PccMetadata> = mutableListOf()

        save.globalNpcList.map { it.value.tilesetMetadata }
                .filter { it is PccTilesetMetadata }
                .forEach { pccMetadataList.addAll((it as PccTilesetMetadata).pccMetadata) }

        unitSchemaIndex.map { it.tilesetMetadata }
                .filter { it is PccTilesetMetadata }
                .forEach { pccMetadataList.addAll((it as PccTilesetMetadata).pccMetadata) }

        pccManager.loadPccTextures(pccMetadataList)
    }
}

class AdvGameInitializer {
    fun initializeGlobals(): AdvGameGlobals {
        val schemaList = initializeSchemaList()
        val weaponSchemaIndex = WeaponSchemaIndex(Gdx.files.internal("assets/data/weapons.csv"))
        val armorSchemaIndex = ArmorSchemaIndex(Gdx.files.internal("assets/data/armor.csv"))
        val itemSchemaIndex = ItemSchemaIndex(Gdx.files.internal("assets/data/items.csv"))
        val skillIndex = SkillIndex(Gdx.files.internal("assets/data/skills.csv"))
        val tilesetList = initializeMapTilesetList()
        val textureManager = TextureManager()
        textureManager.loadAllTextures()
        val pccManager = PccManager()
        val animatedTilesetManager = AnimatedTilesetManager()
        val animatedUnitTilesets = schemaList.filter { it.tilesetMetadata is EnvObjTilesetMetadata.AnimatedUnitTilesetMetadata }
                .map { (it.tilesetMetadata as EnvObjTilesetMetadata.AnimatedUnitTilesetMetadata).filename }
        animatedTilesetManager.loadTextures(animatedUnitTilesets)
        val worldMap = initializeWorldMap()
        return AdvGameGlobals(null, ProgressionBackend(), SaveManager(), schemaList,
                weaponSchemaIndex, armorSchemaIndex, itemSchemaIndex, skillIndex,
                tilesetList, textureManager, pccManager, animatedTilesetManager, worldMap)
    }

    private fun initializeWorldMap(): WorldMap {
        val worldMap = WorldMap(mutableMapOf())
        worldMap.villageLocations["Lagos Village"] = WorldMapLocation(200, 100)
        worldMap.villageLocations["Lagos Forest"] = WorldMapLocation(100, 200)
        return worldMap
    }

    private fun initializeSchemaList(): UnitSchemaIndex {
        val schemaList = UnitSchemaIndex()
        // Stats:
        // HP, MP, STR, DEX, CON, INT, RES, AVD, MOV
        schemaList.addSchema("Human", UnitStats(50, 20, 10, 10, 10, 10, 10, 0, 10),
                Resistances(),
                0,
                EnvObjTilesetMetadata.NONE)
        schemaList.addSchema("Brown Rat", UnitStats(60, 10, 8, 7, 10, 5, 5, 5, 12),
                Resistances().copy(blind = Resistance.WEAK, head = Resistance.WEAK),
                10,
                EnvObjTilesetMetadata.AnimatedUnitTilesetMetadata("brown_rat.png"))
        schemaList.addSchema("Black Butterfly", UnitStats(35, 30, 5, 5, 20, 15, 5, 15, 12),
                Resistances().copy(lightning = Resistance.WEAK, poison = Resistance.WEAK, blind = Resistance.RESIST),
                12,
                EnvObjTilesetMetadata.AnimatedUnitTilesetMetadata("black_butterfly.png"))
        schemaList.addSchema("Killer Rabbit", UnitStats(80, 10, 12, 10, 12, 5, 5, 0, 14),
                Resistances().copy(ice = Resistance.WEAK, leg = Resistance.WEAK),
                17,
                EnvObjTilesetMetadata.AnimatedUnitTilesetMetadata("rabbit.png"))
        schemaList.addSchema("Slime", UnitStats(100, 30, 8, 5, 20, 15, 5, 0, 7),
                Resistances().copy(fire = Resistance.WEAK, ice = Resistance.RESIST, poison = Resistance.RESIST, acid = Resistance.RESIST),
                21,
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
