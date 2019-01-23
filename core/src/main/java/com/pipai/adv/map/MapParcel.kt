package com.pipai.adv.map

import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.utils.AutoIncrementIdMap

class MapParcel(val cells: Map<GridPosition, BattleMapCell>,
                val width: Int, val height: Int) {

    companion object Factory {
        fun readMapParcel(resolver: FileHandleResolver, filename: String, envObjList: AutoIncrementIdMap<EnvObject>): MapParcel {
            val cells: MutableMap<GridPosition, BattleMapCell> = mutableMapOf()
            val loader = TmxMapLoader(resolver)
            val parcelTmx = loader.load(filename)
            val tileLayer = parcelTmx.layers[0] as TiledMapTileLayer
            for (x in 0 until tileLayer.width) {
                for (y in 0 until tileLayer.height) {
                    val cell = tileLayer.getCell(x, y)
                    if (cell != null) {
                        val envObj = parseFullEnvObj(cell)
                        val envObjId = envObj?.let { envObjList.add(it) }
                        cells[GridPosition(x, y)] = BattleMapCell(
                                envObjId,
                                mutableListOf(),
                                mutableListOf(),
                                mutableListOf())
                    }
                }
            }
            return MapParcel(cells, tileLayer.width, tileLayer.height)
        }

        private fun parseFullEnvObj(cell: TiledMapTileLayer.Cell): EnvObject? {
            val fullEnvObjType = MapParcelTileType.values().find { it.encoding == cell.tile.id }
            return when (fullEnvObjType) {
                MapParcelTileType.FULL_COVER -> FullWall(FullWallType.SOLID)
                MapParcelTileType.HALF_COVER -> FullWall(FullWallType.SOLID)
                MapParcelTileType.WALL -> FullWall(FullWallType.SOLID)
                MapParcelTileType.TREE -> FullWall(FullWallType.SOLID)
                else -> null
            }
        }
    }
}
