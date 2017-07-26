package com.pipai.adv.artemis.components

import com.artemis.Component
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.tiles.TilePosition
import com.pipai.adv.tiles.Tileset

class PccComponent : Component() {
    val pccs: MutableList<PccMetadata> = mutableListOf()
}

class NpcTileComponent : Component() {
    lateinit var tileset: Tileset
    lateinit var tilePosition: TilePosition
}
