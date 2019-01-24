package com.pipai.adv.map

import com.pipai.adv.SchemaMetadata
import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.generators.OpenTerrainGenerator
import com.pipai.adv.domain.Npc
import com.pipai.adv.domain.UnitSkill
import com.pipai.adv.index.UnitSchemaIndex
import com.pipai.adv.index.WeaponSchemaIndex
import com.pipai.adv.tiles.MapTileType
import com.pipai.adv.tiles.MapTileset
import com.pipai.adv.tiles.TileDescriptor
import com.pipai.adv.tiles.TilePosition
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.adv.utils.RNG

interface MapGenerator {
    fun generate(schemas: UnitSchemaIndex, weapons: WeaponSchemaIndex,
                 npcList: AutoIncrementIdMap<Npc>, envObjList: AutoIncrementIdMap<EnvObject>, party: List<Int>,
                 width: Int, height: Int, tileset: MapTileset): BattleMap
}

class GuildMapGenerator : MapGenerator {

    override fun generate(schemas: UnitSchemaIndex, weapons: WeaponSchemaIndex,
                          npcList: AutoIncrementIdMap<Npc>, envObjList: AutoIncrementIdMap<EnvObject>, party: List<Int>,
                          width: Int, height: Int, tileset: MapTileset): BattleMap {
        val map = BattleMap.createBattleMap(width, height)
        generateGround(map)
        generateGroundDeco(map, 4, tileset)

        var currentY = 1
        for (index in party) {
            map.getCell(1, currentY).fullEnvObjId = envObjList.add(NpcEnvObject(party[index], Team.PLAYER, npcList.get(party[index])!!.tilesetMetadata))
            currentY++
        }
        generateWallBoundary(envObjList, map)
        return map
    }

}

class TestMapGenerator : MapGenerator {

    override fun generate(schemas: UnitSchemaIndex, weapons: WeaponSchemaIndex,
                          npcList: AutoIncrementIdMap<Npc>, envObjList: AutoIncrementIdMap<EnvObject>, party: List<Int>,
                          width: Int, height: Int, tileset: MapTileset): BattleMap {
        val generator = OpenTerrainGenerator()
        val map = generator.generate(envObjList, width, height)
        generateGround(map)
        generateGroundDeco(map, 4, tileset)

        var currentY = 1
        for (npcId in party) {
            map.getCell(1, currentY).fullEnvObjId = envObjList.add(NpcEnvObject(npcId, Team.PLAYER, npcList.get(npcId)!!.tilesetMetadata))
            currentY++
        }

        generatePod(GridPosition(RNG.nextInt((width - 4) / 2) + 4, RNG.nextInt(height - 10) + 6), map, schemas, weapons, npcList, envObjList)
        generatePod(GridPosition(RNG.nextInt((width - 4) / 2) + width / 2, RNG.nextInt(height - 10) + 6), map, schemas, weapons, npcList, envObjList)

        generateWallBoundary(envObjList, map)
        map.getCell(8, 8).fullEnvObjId = envObjList.add(FullWall(FullWallType.WALL))
        return map
    }

}

private fun generateGround(map: BattleMap) {
    for (column in map.cells) {
        for (cell in column) {
            cell.backgroundTiles.add(MapCellTileInfo(MapTileType.GROUND, 0))
        }
    }
}

private fun generateGroundDeco(map: BattleMap, sparseness: Int, tileset: MapTileset) {
    val decosAmount = tileset.tilePositions(MapTileType.GROUND_DECO).size

    val generatorWidth = map.width + map.width % sparseness
    val generatorHeight = map.height + map.height % sparseness

    for (x in 0..generatorWidth step sparseness) {
        for (y in 0..generatorHeight step sparseness) {
            val decoX = RNG.nextInt(sparseness) + x
            val decoY = RNG.nextInt(sparseness) + y
            if (decoX < map.width && decoY < map.height) {
                map.cells[decoX][decoY].backgroundTiles.add(
                        MapCellTileInfo(MapTileType.GROUND_DECO, RNG.nextInt(decosAmount)))
            }
        }
    }
}

private fun generateWallBoundary(envObjList: AutoIncrementIdMap<EnvObject>, map: BattleMap) {
    for (x in 0 until map.width) {
        if (x > 5 || x == 0) {
            map.getCell(x, 0).fullEnvObjId = envObjList.add(FullWall(FullWallType.WALL))
        }
        map.getCell(x, map.height - 1).fullEnvObjId = envObjList.add(FullWall(FullWallType.WALL))
    }
    for (y in 1 until map.height - 1) {
        map.getCell(0, y).fullEnvObjId = envObjList.add(FullWall(FullWallType.WALL))
        map.getCell(map.width - 1, y).fullEnvObjId = envObjList.add(FullWall(FullWallType.WALL))
    }
}

private fun generatePod(position: GridPosition, map: BattleMap, schemas: UnitSchemaIndex, weapons: WeaponSchemaIndex,
                        npcList: AutoIncrementIdMap<Npc>, envObjList: AutoIncrementIdMap<EnvObject>) {

    val defaultMelee = weapons.getWeaponSchema("Monster Melee")!!
    val defaultRanged = weapons.getWeaponSchema("Monster Ranged")!!

    val podSchemas = generatePodSchemas(schemas).shuffled()
    val podPositions = generatePodPositions(position, podSchemas.size).shuffled()

    val schemaPositions = podSchemas.zip(podPositions)
    schemaPositions.forEach {
        val schema = it.first
        val schemaPosition = it.second
        val weaponSchema = when (schema.schema.name) {
            "Slime" -> defaultRanged
            "Brown Rat" -> defaultMelee
            "Killer Rabbit" -> defaultMelee
            "Brown Butterfly" -> defaultMelee
            else -> defaultMelee
        }
        val skills = schema.schema.enemySkills.map { UnitSkill(1, it) }
        val id = npcList.add(Npc(UnitInstance(schema.schema, schema.schema.name, weaponSchema, skills), schema.tilesetMetadata))
        map.getCell(schemaPosition).fullEnvObjId = envObjList.add(NpcEnvObject(id, Team.AI, schema.tilesetMetadata))
    }
}

private fun generatePodSchemas(schemas: UnitSchemaIndex): List<SchemaMetadata> {
    val potentialPods: MutableList<List<SchemaMetadata>> = mutableListOf(
            listOf(schemas.getSchema("Slime"),
                    schemas.getSchema("Slime"),
                    schemas.getSchema("Slime")),
            listOf(schemas.getSchema("Slime"),
                    schemas.getSchema("Brown Rat"),
                    schemas.getSchema("Brown Rat"),
                    schemas.getSchema("Black Butterfly")),
            listOf(schemas.getSchema("Brown Rat"),
                    schemas.getSchema("Brown Rat"),
                    schemas.getSchema("Brown Rat"),
                    schemas.getSchema("Black Butterfly"),
                    schemas.getSchema("Black Butterfly")),
            listOf(schemas.getSchema("Killer Rabbit"),
                    schemas.getSchema("Killer Rabbit"),
                    schemas.getSchema("Black Butterfly"),
                    schemas.getSchema("Black Butterfly"))
    )
    return potentialPods[RNG.nextInt(potentialPods.size)]
}

private fun generatePodPositions(position: GridPosition, amount: Int): List<GridPosition> {
    val potentialPositions: MutableList<GridPosition> = mutableListOf(
            position,
            position.copy(x = position.x + 1),
            position.copy(x = position.x - 1),
            position.copy(y = position.y + 1),
            position.copy(y = position.y - 1),
            GridPosition(position.x + 1, position.y + 1),
            GridPosition(position.x + 1, position.y - 1),
            GridPosition(position.x - 1, position.y + 1),
            GridPosition(position.x - 1, position.y + 1))

    val positions: MutableList<GridPosition> = mutableListOf()
    for (i in 0 until amount) {
        val index = RNG.nextInt(potentialPositions.size)
        positions.add(potentialPositions.removeAt(index))
    }
    return positions
}
