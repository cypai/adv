package com.pipai.adv.artemis.components

import com.artemis.Component
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.tiles.TilePosition
import com.pipai.adv.tiles.Tileset

class PccComponent : Component() {
    val pccs: MutableList<PccMetadata> = mutableListOf()

    var direction = Direction.S
}

class NpcTileComponent : Component() {
    lateinit var tileset: Tileset
    lateinit var tilePosition: TilePosition
}

class AnimationFramesComponent : Component() {
    var frame = 0
    var frameMax = 0
    var t = 0
    var tMax = 0
}
