package com.pipai.adv.map

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.MapCellTileInfo
import com.pipai.adv.tiles.MapTileType
import com.pipai.adv.tiles.MapTileset
import com.pipai.adv.utils.RNG
import com.pipai.adv.backend.battle.domain.FullEnvironmentObject.NpcEnvironmentObject

interface MapGenerator {
    fun generate(party: List<Int>, width: Int, height: Int, tileset: MapTileset): BattleMap
}

class TestMapGenerator : MapGenerator {

    override fun generate(party: List<Int>, width: Int, height: Int, tileset: MapTileset): BattleMap {
        val map = BattleMap.Factory.createBattleMap(width, height)
        generateGround(map)
        generateGroundDeco(map, 4, tileset)
        map.getCell(1, 1).fullEnvironmentObject = NpcEnvironmentObject(party[0])
        map.getCell(1, 2).fullEnvironmentObject = NpcEnvironmentObject(party[1])
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
