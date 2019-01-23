package com.pipai.adv.domain

import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.adv.utils.DeepCopyable

data class Npc(val unitInstance: UnitInstance,
               val tilesetMetadata: EnvObjTilesetMetadata) : DeepCopyable<Npc> {

    override fun deepCopy() = copy(unitInstance.deepCopy(), tilesetMetadata.deepCopy())
}
