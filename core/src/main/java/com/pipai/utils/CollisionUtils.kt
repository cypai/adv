package com.pipai.utils

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.pipai.adv.artemis.components.CollisionBounds
import com.pipai.adv.artemis.components.CollisionBounds.CollisionBoundingBox

object CollisionUtils {

    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return Math.sqrt((dx * dx).toDouble() + (dy * dy).toDouble()).toFloat()
    }

    fun overlaps(x1: Float, y1: Float, bounds1: CollisionBounds,
                 x2: Float, y2: Float, bounds2: CollisionBounds): Boolean {

        return when (bounds1) {
            is CollisionBoundingBox -> when (bounds2) {
                is CollisionBoundingBox -> {
                    Intersector.overlaps(Rectangle(x1 + bounds1.xOffset, y1 + bounds1.yOffset, bounds1.width, bounds1.height),
                            Rectangle(x2 + bounds2.xOffset, y2 + bounds2.yOffset, bounds2.width, bounds2.height))
                }
            }
        }
    }

    fun minimumTranslationVector(x1: Float, y1: Float, bounds1: CollisionBounds,
                                 x2: Float, y2: Float, bounds2: CollisionBounds): Vector2 = when (bounds1) {

        is CollisionBoundingBox -> when (bounds2) {
            is CollisionBoundingBox -> {
                minimumTranslationVector(Rectangle(x1 + bounds1.xOffset, y1 + bounds1.yOffset, bounds1.width, bounds1.height),
                        Rectangle(x2 + bounds2.xOffset, y2 + bounds2.yOffset, bounds2.width, bounds2.height))
            }
        }
    }

    fun minimumTranslationVector(rect1: Rectangle, rect2: Rectangle): Vector2 {
        val mtv = Vector2(0f, 0f)

        if (!Intersector.overlaps(rect1, rect2)) {
            return mtv
        }

        val left = rect1.x + rect1.width - rect2.x
        val right = rect2.x + rect2.width - rect1.x

        if (left < right) {
            mtv.x = -left
        } else {
            mtv.x = right
        }

        val down = rect1.y + rect1.height - rect2.y
        val up = rect2.y + rect2.height - rect1.y

        if (down < up) {
            mtv.y = -down
        } else {
            mtv.y = up
        }

        if (Math.abs(mtv.x) <= Math.abs(mtv.y)) {
            mtv.y = 0f
        } else {
            mtv.x = 0f
        }

        return mtv
    }
}
