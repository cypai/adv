package com.pipai.adv.artemis.components

import com.artemis.Component

class CollisionComponent : Component() {
    lateinit var bounds: CollisionBounds
}

sealed class CollisionBounds {
    data class CollisionBoundingBox(var xOffset: Float, var yOffset: Float, var width: Float, var height: Float) : CollisionBounds() {
        constructor(cDrawable: DrawableComponent) : this(
                if (cDrawable.centered) -cDrawable.width / 2f else 0f,
                if (cDrawable.centered) -cDrawable.height / 2f else 0f,
                cDrawable.width,
                cDrawable.height)
    }
}

class WallCollisionFlagComponent() : Component()
