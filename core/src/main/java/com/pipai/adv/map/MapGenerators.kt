package com.pipai.adv.map

import com.pipai.adv.SchemaList
import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.domain.FullEnvObject.NpcEnvObject
import com.pipai.adv.npc.Npc
import com.pipai.adv.npc.NpcList
import com.pipai.adv.tiles.MapTileType
import com.pipai.adv.tiles.MapTileset
import com.pipai.adv.utils.RNG

interface MapGenerator {
    fun generate(schemas: SchemaList, npcList: NpcList, party: List<Int>, width: Int, height: Int, tileset: MapTileset): BattleMap
}

class TestMapGenerator : MapGenerator {

    override fun generate(schemas: SchemaList, npcList: NpcList, party: List<Int>, width: Int, height: Int, tileset: MapTileset): BattleMap {
        val map = BattleMap.Factory.createBattleMap(width, height)
        generateGround(map)
        generateGroundDeco(map, 4, tileset)

        var currentY = 1
        for (index in party) {
            map.getCell(1, currentY).fullEnvObject = NpcEnvObject(party[index], Team.PLAYER, npcList.getNpc(party[index])!!.tilesetMetadata)
            currentY++
        }

        val slimeSchema = schemas.getSchema("Slime")
        val slimeId = npcList.addNpc(Npc(UnitInstance(slimeSchema.schema, "Slime"), slimeSchema.tilesetMetadata))
        map.getCell(3, 3).fullEnvObject = NpcEnvObject(slimeId, Team.AI, slimeSchema.tilesetMetadata)
        generateWallBoundary(map)
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

private fun generateWallBoundary(map: BattleMap) {
    for (x in 0 until map.width) {
        map.getCell(x, 0).fullEnvObject = FullEnvObject.FULL_WALL
        map.getCell(x, map.height - 1).fullEnvObject = FullEnvObject.FULL_WALL
    }
    for (y in 1 until map.height - 1) {
        map.getCell(0, y).fullEnvObject = FullEnvObject.FULL_WALL
        map.getCell(map.width - 1, y).fullEnvObject = FullEnvObject.FULL_WALL
    }
}
