package com.pipai.adv.artemis.components

import com.artemis.Component
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
