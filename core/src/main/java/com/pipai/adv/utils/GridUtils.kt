package com.pipai.adv.utils

import com.badlogic.gdx.math.Vector2
import com.pipai.adv.backend.battle.domain.GridPosition

object GridUtils {

    fun localToGridPosition(x: Float, y: Float, tileSize: Float): GridPosition {
        return GridPosition((x / tileSize).toInt(), (y / tileSize).toInt())
    }

    fun gridPositionToLocal(gridPosition: GridPosition, tileSize: Float): Vector2 {
        return Vector2(gridPosition.x.toFloat() * tileSize, gridPosition.y.toFloat() * tileSize)
    }

}
