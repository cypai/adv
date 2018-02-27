package com.pipai.adv.artemis.components

import com.artemis.Component
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.engine.BattleBackend

class SideUiBoxComponent : Component() {
    var orientation = SideUiBoxOrientation.RIGHT
    var npcId = 0
    var disabled = false
    lateinit var name: String
    var hp = 0
    var tp = 0
    var onFieldPortrait: EnvObjTilesetMetadata? = null
    var portrait: Drawable? = null

    fun setToNpc(npcId: Int, backend: BattleBackend) {
        this.npcId = npcId
        val npc = backend.getBattleState().npcList.getNpc(npcId)
        if (npc != null) {
            name = npc.unitInstance.nickname
            hp = npc.unitInstance.hp
            tp = npc.unitInstance.tp
            onFieldPortrait = npc.tilesetMetadata
        }
    }
}

enum class SideUiBoxOrientation {
    LEFT, RIGHT
}

class UnitBottomUiComponent : Component() {
    var npcId = 0
    var hp = 0
    var tp = 0
}
