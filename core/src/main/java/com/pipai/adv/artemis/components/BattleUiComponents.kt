package com.pipai.adv.artemis.components

import com.artemis.Component
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.engine.BattleBackend

class SideUiBoxComponent : Component() {
    var orientation = SideUiBoxOrientation.PORTRAIT_LEFT
    var npcId = 0
    var disabled = false
    lateinit var name: String
    var hp = 0
    var hpMax = 0
    var tp = 0
    var tpMax = 0
    var onFieldPortrait: EnvObjTilesetMetadata? = null
    var portrait: Drawable? = null

    fun setToNpc(npcId: Int, backend: BattleBackend) {
        this.npcId = npcId
        val npc = backend.getBattleState().npcList.getNpc(npcId)
        if (npc != null) {
            name = npc.unitInstance.nickname
            hp = npc.unitInstance.hp
            hpMax = npc.unitInstance.schema.baseStats.hpMax
            tp = npc.unitInstance.tp
            tpMax = npc.unitInstance.schema.baseStats.tpMax
            onFieldPortrait = npc.tilesetMetadata
        }
    }
}

enum class SideUiBoxOrientation {
    PORTRAIT_RIGHT, PORTRAIT_LEFT
}

class UnitBottomUiComponent : Component() {
    var npcId = 0
    var hp = 0
    var tp = 0
}
