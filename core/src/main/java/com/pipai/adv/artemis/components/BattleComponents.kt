package com.pipai.adv.artemis.components

import com.artemis.Component
import com.pipai.adv.backend.battle.engine.BattleBackend

class BattleBackendComponent : Component() {
    lateinit var backend: BattleBackend
}

class NpcSelectionComponent : Component()

class ClickableComponent : Component() {
    var width = 0
    var height = 0
    var xOffset = 0
    var yOffset = 0
}
