package com.pipai.adv.artemis.components

import com.artemis.Component
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.tiles.TilePosition
import com.pipai.adv.tiles.Tileset

class EnvObjTileComponent : Component() {
    lateinit var tilesetMetadata: EnvObjTilesetMetadata

    var direction = Direction.S
}
