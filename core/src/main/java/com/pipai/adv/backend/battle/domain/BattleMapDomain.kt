package com.pipai.adv.backend.battle.domain

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.google.common.base.Preconditions
import com.pipai.adv.tiles.MapTileType
import com.pipai.adv.tiles.PccMetadata
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
        Preconditions.checkElementIndex(x, width)
        Preconditions.checkElementIndex(y, height)
        return cells[x][y]
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

sealed class FullEnvObject : DeepCopyable<FullEnvObject> {

    abstract fun getTilesetMetadata(): EnvObjTilesetMetadata

    companion object {
        @JvmField
        val SOLID_FULL_WALL = FullEnvObject.FullWall(FullEnvObject.FullWallType.SOLID)

        @JvmField
        val FULL_WALL = FullEnvObject.FullWall(FullEnvObject.FullWallType.WALL)
    }

    data class NpcEnvObject(val npcId: Int,
                            var team: Team,
                            private val envObjTilesetMetadata: EnvObjTilesetMetadata) : FullEnvObject() {

        override fun deepCopy() = copy(npcId)
        override fun getTilesetMetadata() = envObjTilesetMetadata
    }

    data class ChestEnvObject(val item: InventoryItem,
                              private val envObjTilesetMetadata: EnvObjTilesetMetadata) : FullEnvObject() {

        override fun deepCopy() = copy(item.deepCopy())
        override fun getTilesetMetadata() = envObjTilesetMetadata
    }

    data class DestructibleEnvObject(val type: DestructibleEnvObjectType, var hp: Int,
                                     private val envObjTilesetMetadata: EnvObjTilesetMetadata) : FullEnvObject() {

        override fun deepCopy() = copy()
        override fun getTilesetMetadata() = envObjTilesetMetadata
    }

    enum class DestructibleEnvObjectType(val defaultMinHp: Int, val defaultMaxHp: Int) {
        TREE(100, 120), ROCK(70, 100), BOULDER(170, 200)
    }

    data class FullWall(val type: FullWallType) : FullEnvObject() {
        override fun deepCopy() = copy()
        override fun getTilesetMetadata() = EnvObjTilesetMetadata.MapTilesetMetadata(MapTileType.WALL)
    }

    enum class FullWallType {
        SOLID, WALL
    }
}

sealed class EnvObject : DeepCopyable<EnvObject> {
    abstract fun getTilesetMetadata(): EnvObjTilesetMetadata

    class InventoryItemEnvObject(val item: InventoryItem)
    // Other potentially interesting ideas:
    // TrapEnvironmentObject
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
        var fullEnvObject: FullEnvObject?,
        val envObjects: MutableList<EnvObject>,
        val specialFlags: MutableList<BattleMapCellSpecialFlag>,
        val backgroundTiles: MutableList<MapCellTileInfo>) : DeepCopyable<BattleMapCell> {

    override fun deepCopy() = BattleMapCell(fullEnvObject?.deepCopy(),
            envObjects.map { it.deepCopy() }.toMutableList(),
            specialFlags.toMutableList(),
            backgroundTiles.toMutableList())
}

data class MapCellTileInfo(val tileType: MapTileType, val index: Int)
