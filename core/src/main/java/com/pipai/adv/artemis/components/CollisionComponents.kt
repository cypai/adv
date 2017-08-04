package com.pipai.adv.artemis.components

import com.artemis.Component

class CollisionComponent : Component() {
    lateinit var bounds: CollisionBounds
}

sealed class CollisionBounds {
    data class CollisionBoundingBox(var xOffset: Float, var yOffset: Float, var width: Float, var height: Float) : CollisionBounds()
}

class WallCollisionComponent() : Component()
