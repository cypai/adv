package com.pipai.adv.npc

import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.adv.tiles.EnvObjTilesetType
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.tiles.TilePosition
import com.pipai.utils.DeepCopyable

class NpcList {
    private var nextId = 0
    private val npcs: MutableMap<Int, Npc> = mutableMapOf()

    fun addNpc(npc: Npc): Int {
        val id = nextId
        npcs.put(id, npc)
        nextId += 1
        return id
    }

    fun setNpc(npc: Npc, id: Int) {
        npcs.put(id, npc)
        if (id >= nextId) {
            nextId = id + 1
        }
    }

    fun removeNpc(id: Int) {
        if (npcExists(id)) {
            npcs.remove(id)
        }
    }

    fun npcExists(id: Int): Boolean = npcs.containsKey(id)

    fun getNpc(id: Int): Npc {
        if (!npcExists(id)) {
            throw IllegalArgumentException("NPC id ${id} does not exist")
        }
        return npcs.get(id)!!
    }
}

data class Npc(val unitInstance: UnitInstance,
               val tileType: EnvObjTilesetType,
               val atlasTilesetPosition: TilePosition?,
               val pccMetadata: List<PccMetadata>?) : DeepCopyable<Npc> {

    override fun deepCopy() = copy(unitInstance.deepCopy(), tileType, atlasTilesetPosition, pccMetadata?.toList())
}
