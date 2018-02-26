package com.pipai.adv.artemis.components

import com.artemis.Component
import com.artemis.annotations.DelayedComponentRemoval
import com.pipai.adv.backend.battle.engine.BattleBackend

class BattleBackendComponent : Component() {
    lateinit var backend: BattleBackend
}

@DelayedComponentRemoval
class NpcIdComponent : Component() {
    var npcId = 0
}

class PlayerUnitComponent : Component() {
    var index = 0
}
