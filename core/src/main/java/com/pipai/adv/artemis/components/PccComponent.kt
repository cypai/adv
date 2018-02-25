package com.pipai.adv.artemis.components

import com.artemis.Component
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.tiles.PccMetadata

class PccComponent : Component() {
    lateinit var pcc: List<PccMetadata>
    lateinit var direction: Direction
}
