package com.pipai.adv.utils

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.GridPosition

object GridUtils {

    fun localToGridPosition(x: Float, y: Float, tileSize: Float): GridPosition {
        return GridPosition((x / tileSize).toInt(), (y / tileSize).toInt())
    }

    fun localToGridPosition(position: Vector2, tileSize: Float): GridPosition {
        return localToGridPosition(position.x, position.y, tileSize)
    }

    fun gridPositionToLocal(gridPosition: GridPosition, tileSize: Float): Vector2 {
        return Vector2(gridPosition.x.toFloat() * tileSize, gridPosition.y.toFloat() * tileSize)
    }

    fun gridPositionToLocalOffset(gridPosition: GridPosition, tileSize: Float, offsetX: Float, offsetY: Float): Vector2 {
        return Vector2(gridPosition.x.toFloat() * tileSize + offsetX, gridPosition.y.toFloat() * tileSize + offsetY)
    }

    fun gridDistance(pos1: GridPosition, pos2: GridPosition): Float {
        return MathUtils.distance(pos1.x.toFloat(), pos1.y.toFloat(), pos2.x.toFloat(), pos2.y.toFloat()).toFloat()
    }

    fun gridDistance2(pos1: GridPosition, pos2: GridPosition): Int {
        return MathUtils.distance2(pos1.x, pos1.y, pos2.x, pos2.y)
    }

    fun boundaries(bottomLeft: GridPosition, topRight: GridPosition): Array<GridPosition> {
        val arr = Array<GridPosition>()
        for (x in bottomLeft.x..topRight.x) {
            arr.add(GridPosition(x, bottomLeft.y))
            arr.add(GridPosition(x, topRight.y))
        }
        for (y in bottomLeft.y + 1 until topRight.y) {
            arr.add(GridPosition(bottomLeft.x, y))
            arr.add(GridPosition(topRight.x, y))
        }
        return arr
    }

    fun gridInRadius(center: GridPosition, radius: Int, minX: Int, minY: Int, maxX: Int, maxY: Int): Array<GridPosition> {
        val arr = Array<GridPosition>()
        arr.add(center)
        val radius2 = radius * radius
        for (x in Math.max(minX, center.x - radius)..Math.min(maxX, center.x + radius)) {
            for (y in Math.max(minY, center.y - radius)..Math.min(maxY, center.y + radius)) {
                if (MathUtils.distance2(x, y, center.x, center.y) <= radius2) {
                    arr.add(GridPosition(x, y))
                }
            }
        }
        return arr
    }

    fun tilesInFront(start: GridPosition, end: GridPosition): Array<GridPosition> {
        val direction = DirectionUtils.directionFor(
                start.x.toFloat(), start.y.toFloat(),
                end.x.toFloat(), end.y.toFloat())
        return when (direction) {
            Direction.N -> ArrayUtils.libgdxArrayOf(GridPosition(start.x, start.y - 1))
            Direction.S -> ArrayUtils.libgdxArrayOf(GridPosition(start.x, start.y + 1))
            Direction.W -> ArrayUtils.libgdxArrayOf(GridPosition(start.x + 1, start.y))
            Direction.E -> ArrayUtils.libgdxArrayOf(GridPosition(start.x - 1, start.y))
            Direction.NE -> ArrayUtils.libgdxArrayOf(GridPosition(start.x, start.y - 1), GridPosition(start.x - 1, start.y))
            Direction.NW -> ArrayUtils.libgdxArrayOf(GridPosition(start.x, start.y - 1), GridPosition(start.x + 1, start.y))
            Direction.SE -> ArrayUtils.libgdxArrayOf(GridPosition(start.x, start.y + 1), GridPosition(start.x - 1, start.y))
            Direction.SW -> ArrayUtils.libgdxArrayOf(GridPosition(start.x, start.y + 1), GridPosition(start.x + 1, start.y))
        }
    }

    fun isNeighbor(pos1: GridPosition, pos2: GridPosition): Boolean {
        return (pos1.x - pos2.x) in -1..1 && (pos1.y - pos2.y) in -1..1
    }

}
