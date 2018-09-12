package com.pipai.adv.artemis.components

import com.artemis.Component

class CollisionComponent : Component() {
    lateinit var bounds: CollisionBounds
}

sealed class CollisionBounds {
    data class CollisionBoundingBox(var xOffset: Float, var yOffset: Float, var width: Float, var height: Float) : CollisionBounds() {
        constructor(width: Float, height: Float, centered: Boolean) : this(
                if (centered) -width / 2f else 0f,
                if (centered) -height / 2f else 0f,
                width,
                height)
    }
}

class WallCollisionFlagComponent() : Component()
