package com.pipai.adv.backend.battle.domain

import com.google.common.base.Preconditions
import com.pipai.utils.DeepCopyable
import com.pipai.utils.deepCopy

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
        return cells.get(x).get(y)
    }

    companion object Factory {
        fun createBattleMap(width: Int, height: Int): BattleMap {
            val rows: MutableList<List<BattleMapCell>> = mutableListOf()
            for (y in 0 until height) {
                val row: MutableList<BattleMapCell> = mutableListOf()
                for (x in 0 until width) {
                    row.add(BattleMapCell(null, mutableListOf(), mutableListOf()))
                }
                rows.add(row)
            }
            return BattleMap(width, height, rows.toList())
        }
    }
}

sealed class FullEnvironmentObject : DeepCopyable<FullEnvironmentObject> {

    data class BattleUnitEnvironmentObject(val battleUnit: BattleUnit) : FullEnvironmentObject() {
        override fun deepCopy() = copy(battleUnit.deepCopy())
    }

    data class ChestEnvironmentObject(val item: InventoryItem) : FullEnvironmentObject() {
        override fun deepCopy() = copy(item.deepCopy())
    }

    data class DestructibleEnvironmentObject(val type: DestructibleEnvironmentObjectType, var hp: Int) : FullEnvironmentObject() {
        override fun deepCopy() = copy()
    }

    enum class DestructibleEnvironmentObjectType(val defaultMinHp: Int, val defaultMaxHp: Int) {
        TREE(100, 120), ROCK(70, 100), BOULDER(170, 200)
    }

    data class FullWall(val type: FullWallType) : FullEnvironmentObject() {
        override fun deepCopy() = copy()
    }

    enum class FullWallType {
        SOLID, WALL
    }
}

sealed class EnvironmentObject : DeepCopyable<EnvironmentObject> {
    class InventoryItemEnvironmentObject(val item: InventoryItem)
    // Other potentially interesting ideas:
    // TrapEnvironmentObject
}

sealed class BattleMapCellSpecialFlag {
    data class StartingCell(val number: Int) : BattleMapCellSpecialFlag()
    class Exit : BattleMapCellSpecialFlag()
}

data class BattleMapCell(
        var fullEnvironmentObject: FullEnvironmentObject?,
        val environmentObjects: MutableList<EnvironmentObject>,
        val specialFlags: MutableList<BattleMapCellSpecialFlag>) : DeepCopyable<BattleMapCell> {

    override fun deepCopy() = BattleMapCell(fullEnvironmentObject?.deepCopy(),
            environmentObjects.map { it.deepCopy() }.toMutableList(),
            specialFlags.toMutableList())
}
