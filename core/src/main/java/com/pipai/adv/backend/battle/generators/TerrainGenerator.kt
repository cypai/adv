package com.pipai.adv.backend.battle.generators

import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver
import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.domain.BattleMapCellSpecialFlag.Exit
import com.pipai.adv.backend.battle.engine.domain.CoverType
import com.pipai.adv.map.MapParcel
import com.pipai.adv.tiles.TileDescriptor
import com.pipai.adv.tiles.TilePosition
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.adv.utils.RNG

internal val EXIT = Exit()

interface TerrainGenerator {
    fun generate(envObjList: AutoIncrementIdMap<EnvObject>, width: Int, height: Int): BattleMap
}

class ParcelTerrainGenerator : TerrainGenerator {
    override fun generate(envObjList: AutoIncrementIdMap<EnvObject>, width: Int, height: Int): BattleMap {
        val map = BattleMap.createBattleMap(width, height)
        val mapParcel = MapParcel.Factory.readMapParcel(LocalFileHandleResolver(), "assets/binassets/maps/parcel1.tmx", envObjList)
        placeParcelInMap(map, mapParcel)
        return map
    }

    private fun placeParcelInMap(map: BattleMap, mapParcel: MapParcel) {
        for (x in 0 until mapParcel.width) {
            for (y in 0 until mapParcel.height) {
                val position = GridPosition(x, y)
                val parcelCell = mapParcel.cells[position]
                if (parcelCell != null) {
                    val mapCell = map.getCell(x, y)
                    mapCell.fullEnvObjId = parcelCell.fullEnvObjId
                    mapCell.backgroundTiles.addAll(parcelCell.backgroundTiles)
                    mapCell.envObjIds.addAll(parcelCell.envObjIds)
                    mapCell.specialFlags.addAll(parcelCell.specialFlags)
                }
            }
        }
    }
}

class OpenTerrainGenerator : TerrainGenerator {

    var trees: Int = 50
    var rocks: Int = 0

    override fun generate(envObjList: AutoIncrementIdMap<EnvObject>, width: Int, height: Int): BattleMap {
        val map = BattleMap.createBattleMap(width, height)
        repeat(trees, {
            val tree = DestructibleEnvObject(10, CoverType.FULL, EnvObjTilesetMetadata.SingleTilesetMetadata(TileDescriptor("trees", TilePosition(0, 0))))
            addFullEnvironmentObjectToRandomPosition(envObjList, map, tree)
        })
        repeat(rocks, {
            val rock = DestructibleEnvObject(5, CoverType.HALF, EnvObjTilesetMetadata.NONE)
            addFullEnvironmentObjectToRandomPosition(envObjList, map, rock)
        })
        addExitsToPerimeter(map)
        return map
    }

}

fun addExitsToPerimeter(map: BattleMap) {
    val width = map.width
    val height = map.height
    for (x in 0 until width) {
        map.getCell(x, 0).specialFlags.add(EXIT)
        map.getCell(x, height - 1).specialFlags.add(EXIT)
    }
    for (y in 1 until height - 1) {
        map.getCell(0, y).specialFlags.add(EXIT)
        map.getCell(width - 1, y).specialFlags.add(EXIT)
    }
}

fun addFullEnvironmentObjectToRandomPosition(envObjList: AutoIncrementIdMap<EnvObject>, map: BattleMap, envObj: EnvObject) {
    val envObjId = envObjList.add(envObj)

    val width = map.width
    val height = map.height

    var added = false
    while (!added) {
        val x = RNG.nextInt(width)
        val y = RNG.nextInt(height)
        val cell = map.getCell(x, y)
        if (cell.fullEnvObjId == null) {
            cell.fullEnvObjId = envObjId
            added = true
        }
    }
}
