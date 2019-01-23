package com.pipai.adv.backend.battle.domain

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.pipai.adv.backend.battle.engine.domain.CoverType
import com.pipai.adv.tiles.MapTileType
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.tiles.TileDescriptor
import com.pipai.adv.utils.DeepCopyable
import com.pipai.adv.utils.deepCopy

data class GridPosition(val x: Int, val y: Int)

data class BattleMap internal constructor(val width: Int, val height: Int, val cells: List<List<BattleMapCell>>) :
        DeepCopyable<BattleMap> {

    override fun deepCopy() = copy(cells = deepCopy(cells))

    fun getCell(position: GridPosition): BattleMapCell {
        return getCell(position.x, position.y)
    }

    fun getCell(x: Int, y: Int): BattleMapCell {
        return cells[x][y]
    }

    fun getCellSafe(position: GridPosition): BattleMapCell? {
        return getCellSafe(position.x, position.y)
    }

    fun getCellSafe(x: Int, y: Int): BattleMapCell? {
        return if (x in 0 until width && y in 0 until height) {
            getCell(x, y)
        } else {
            null
        }
    }

    companion object Factory {
        fun createBattleMap(width: Int, height: Int): BattleMap {
            val columns: MutableList<List<BattleMapCell>> = mutableListOf()
            for (x in 0 until width) {
                val column: MutableList<BattleMapCell> = mutableListOf()
                for (y in 0 until height) {
                    column.add(BattleMapCell(null, mutableListOf(), mutableListOf(), mutableListOf()))
                }
                columns.add(column)
            }
            return BattleMap(width, height, columns.toList())
        }
    }
}

interface EnvObject : DeepCopyable<EnvObject> {
    fun getTilesetMetadata(): EnvObjTilesetMetadata
}

data class NpcEnvObject(val npcId: Int, var team: Team,
                        private val envObjTilesetMetadata: EnvObjTilesetMetadata) : EnvObject {

    override fun deepCopy() = copy()
    override fun getTilesetMetadata() = envObjTilesetMetadata
}

data class ChestEnvObject(val item: InventoryItem,
                          private val envObjTilesetMetadata: EnvObjTilesetMetadata) : EnvObject {

    override fun deepCopy() = copy(item = item.deepCopy())
    override fun getTilesetMetadata() = envObjTilesetMetadata
}

data class DestructibleEnvObject(var hp: Int, val coverType: CoverType,
                                 private val envObjTilesetMetadata: EnvObjTilesetMetadata) : EnvObject {

    override fun deepCopy() = copy()
    override fun getTilesetMetadata() = envObjTilesetMetadata
}

data class FullWall(val type: FullWallType) : EnvObject {
    override fun deepCopy() = copy()
    override fun getTilesetMetadata() = EnvObjTilesetMetadata.MapTilesetMetadata(MapTileType.WALL)
}

enum class FullWallType {
    WALL,   // Specifies that this looks like a wall rather than a solid block of rock, etc.
    SOLID   // This is like a large block of rock (for example, when underground, the area that is inaccessible)
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
sealed class EnvObjTilesetMetadata : DeepCopyable<EnvObjTilesetMetadata> {

    companion object {
        @JvmField
        val NONE = NoEnvObjectTilesetMetadata()
    }

    class NoEnvObjectTilesetMetadata : EnvObjTilesetMetadata() {
        override fun deepCopy() = NoEnvObjectTilesetMetadata()
    }

    data class SingleTilesetMetadata(val tileDescriptor: TileDescriptor) : EnvObjTilesetMetadata() {
        override fun deepCopy() = copy()
    }

    data class MapTilesetMetadata(val mapTileType: MapTileType) : EnvObjTilesetMetadata() {
        override fun deepCopy() = copy()
    }

    data class PccTilesetMetadata(val pccMetadata: List<PccMetadata>) : EnvObjTilesetMetadata() {
        override fun deepCopy() = copy()
    }

    data class AnimatedUnitTilesetMetadata(val filename: String) : EnvObjTilesetMetadata() {
        override fun deepCopy() = copy()
    }
}

sealed class BattleMapCellSpecialFlag {
    data class StartingCell(val number: Int) : BattleMapCellSpecialFlag()
    class Exit : BattleMapCellSpecialFlag()
}

data class BattleMapCell(
        var fullEnvObjId: Int?,
        val envObjIds: MutableList<Int>,
        val specialFlags: MutableList<BattleMapCellSpecialFlag>,
        val backgroundTiles: MutableList<MapCellTileInfo>) : DeepCopyable<BattleMapCell> {

    override fun deepCopy() = BattleMapCell(fullEnvObjId,
            envObjIds.toMutableList(),
            specialFlags.toMutableList(),
            backgroundTiles.toMutableList())
}

data class MapCellTileInfo(val tileType: MapTileType, val index: Int)
