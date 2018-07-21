package com.pipai.adv.artemis.components

import com.artemis.Component
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.pipai.adv.tiles.TileDescriptor

class AnimationFramesComponent : Component() {
    var freeze = false
    var frame = 0
    var frameMax = 0
    var t = 0
    var tMax = 0
    var tStartNoise = 0
}

class TileDescriptorComponent : Component() {
    lateinit var descriptor: TileDescriptor
}

class DrawableComponent : Component() {
    lateinit var drawable: Drawable
    var width: Float = 0f
    var height: Float = 0f
    var depth: Int = 0
    var centered = false
}

class ActorComponent : Component() {
    lateinit var actor: Actor
}

class PartialRenderComponent : Component() {
    var widthPercentage: Float = 1f
    var heightPercentage: Float = 1f
}

class PartialRenderHeightInterpolationComponent : Component() {
    lateinit var interpolation: Interpolation
    var t = 0
    var maxT = 0
    var tIncrement = 1

    var start = 1f
    var end = 1f

    var onEnd = PartialRenderHeightInterpolationEndStrategy.REMOVE
    var onEndCallback: (() -> Unit)? = null

    fun heightPercentage() = interpolation.apply(start, end, t.toFloat() / maxT.toFloat())
}

enum class PartialRenderHeightInterpolationEndStrategy {
    REMOVE, DESTROY
}
