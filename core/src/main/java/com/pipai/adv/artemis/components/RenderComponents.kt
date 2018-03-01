package com.pipai.adv.artemis.components

import com.artemis.Component
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
}

class ActorComponent : Component() {
    lateinit var actor: Actor
}
