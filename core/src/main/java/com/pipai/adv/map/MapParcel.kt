package com.pipai.adv.map

import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.pipai.adv.backend.battle.domain.BattleMapCell
import com.pipai.adv.backend.battle.domain.FullEnvObject
import com.pipai.adv.backend.battle.domain.GridPosition

class MapParcel(val cells: Map<GridPosition, BattleMapCell>,
                val width: Int, val height: Int) {

    companion object Factory {
        fun readMapParcel(resolver: FileHandleResolver, filename: String): MapParcel {
            val cells: MutableMap<GridPosition, BattleMapCell> = mutableMapOf()
            val loader = TmxMapLoader(resolver)
            val parcelTmx = loader.load(filename)
            val tileLayer = parcelTmx.layers[0] as TiledMapTileLayer
            for (x in 0 until tileLayer.width) {
                for (y in 0 until tileLayer.height) {
                    val cell = tileLayer.getCell(x, y)
                    if (cell != null) {
                        cells[GridPosition(x, y)] = BattleMapCell(
                                parseFullEnvObj(cell),
                                mutableListOf(),
                                mutableListOf(),
                                mutableListOf())
                    }
                }
            }
            return MapParcel(cells, tileLayer.width, tileLayer.height)
        }

        private fun parseFullEnvObj(cell: TiledMapTileLayer.Cell): FullEnvObject? {
            val fullEnvObjType = MapParcelTileType.values().find { it.encoding == cell.tile.id }
            return when (fullEnvObjType) {
                MapParcelTileType.FULL_COVER -> FullEnvObject.SOLID_FULL_WALL
                MapParcelTileType.HALF_COVER -> FullEnvObject.SOLID_FULL_WALL
                MapParcelTileType.WALL -> FullEnvObject.SOLID_FULL_WALL
                MapParcelTileType.TREE -> FullEnvObject.SOLID_FULL_WALL
                else -> null
            }
        }
    }
}
